package com.astrahealth.backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.request.CreateHealthReadingRequest;
import com.astrahealth.backend.dto.response.HealthReadingResponse;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.Device;
import com.astrahealth.backend.entity.DeviceStatus;
import com.astrahealth.backend.entity.HealthDataSource;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.DeviceRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.security.HealthAccessService;

@Service
@Transactional(readOnly = true)
public class HealthReadingService {

	private static final int MAX_BULK_SIZE = 500;
	private static final Duration ALLOWED_FUTURE_SKEW = Duration.ofMinutes(5);

	private final HealthReadingRepository readingRepository;
	private final AstronautRepository astronautRepository;
	private final DeviceRepository deviceRepository;
	private final HealthAccessService healthAccessService;
	private final AnomalyDetectionService anomalyDetectionService;
	private final Clock clock;

	public HealthReadingService(
			HealthReadingRepository readingRepository,
			AstronautRepository astronautRepository,
			DeviceRepository deviceRepository,
			HealthAccessService healthAccessService,
			AnomalyDetectionService anomalyDetectionService,
			Clock clock) {
		this.readingRepository = readingRepository;
		this.astronautRepository = astronautRepository;
		this.deviceRepository = deviceRepository;
		this.healthAccessService = healthAccessService;
		this.anomalyDetectionService = anomalyDetectionService;
		this.clock = clock;
	}

	@Transactional
	public HealthReadingResponse create(CreateHealthReadingRequest request, Long userId) {
		HealthReading reading = readingRepository.saveAndFlush(save(request, userId));
		anomalyDetectionService.analyze(reading);
		return HealthReadingResponse.from(reading);
	}

	@Transactional
	public List<HealthReadingResponse> ingest(List<CreateHealthReadingRequest> requests, Long userId) {
		if (requests == null || requests.isEmpty() || requests.size() > MAX_BULK_SIZE) {
			throw new InvalidRequestException("Bulk ingestion must contain between 1 and 500 readings.");
		}
		Set<String> eventIds = new HashSet<>();
		List<HealthReading> readings = new ArrayList<>(requests.size());
		for (CreateHealthReadingRequest request : requests) {
			if (request.sourceEventId() != null) {
				String eventId = request.sourceEventId().trim();
				if (!eventIds.add(eventId) || readingRepository.existsBySourceEventId(eventId)) {
					throw new DuplicateResourceException("A reading with this source event ID already exists.");
				}
			}
			readings.add(save(request, userId));
		}
		List<HealthReading> saved = readingRepository.saveAllAndFlush(readings);
		saved.forEach(anomalyDetectionService::analyze);
		return saved.stream().map(HealthReadingResponse::from).toList();
	}

	public HealthReadingResponse get(Long id, Long userId) {
		HealthReading reading = readingRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Health reading not found with ID: " + id));
		healthAccessService.requireAccess(reading.getAstronaut().getId(), userId);
		return HealthReadingResponse.from(reading);
	}

	public Page<HealthReadingResponse> history(
			Long astronautId,
			ReadingType readingType,
			Instant fromTime,
			Instant toTime,
			Long userId,
			Pageable pageable) {
		requireAstronautExists(astronautId);
		healthAccessService.requireAccess(astronautId, userId);
		validateRange(fromTime, toTime);
		return readingRepository.search(astronautId, readingType, fromTime, toTime, pageable)
				.map(HealthReadingResponse::from);
	}

	public List<HealthReadingResponse> latest(Long astronautId, ReadingType readingType, Long userId) {
		requireAstronautExists(astronautId);
		healthAccessService.requireAccess(astronautId, userId);
		if (readingType != null) {
			return readingRepository
					.findFirstByAstronautIdAndReadingTypeOrderByRecordedAtDescIdDesc(astronautId, readingType)
					.map(HealthReadingResponse::from)
					.map(List::of)
					.orElseGet(List::of);
		}
		return java.util.Arrays.stream(ReadingType.values())
				.map(type -> readingRepository
						.findFirstByAstronautIdAndReadingTypeOrderByRecordedAtDescIdDesc(astronautId, type))
				.flatMap(java.util.Optional::stream)
				.map(HealthReadingResponse::from)
				.toList();
	}

	private HealthReading save(CreateHealthReadingRequest request, Long userId) {
		validateReading(request);
		Astronaut astronaut = astronautRepository.findById(request.astronautId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Astronaut not found with ID: " + request.astronautId()));
		healthAccessService.requireAccess(astronaut.getId(), userId);
		if (request.sourceEventId() != null) {
			String eventId = request.sourceEventId().trim();
			if (eventId.isEmpty()) {
				throw new InvalidRequestException("Source event ID cannot be blank.");
			}
			if (readingRepository.existsBySourceEventId(eventId)) {
				throw new DuplicateResourceException("A reading with this source event ID already exists.");
			}
		}

		Device device = null;
		if (request.deviceId() != null) {
			device = deviceRepository.findById(request.deviceId())
					.orElseThrow(() -> new ResourceNotFoundException(
							"Device not found with ID: " + request.deviceId()));
			if (device.getAssignedAstronaut() == null
					|| !astronaut.getId().equals(device.getAssignedAstronaut().getId())) {
				throw new InvalidRequestException("The device is not assigned to the specified astronaut.");
			}
			if (device.getStatus() != DeviceStatus.ACTIVE) {
				throw new InvalidRequestException("Readings cannot be accepted from a non-active device.");
			}
		} else if (request.dataSource() == HealthDataSource.DEVICE) {
			throw new InvalidRequestException("A device ID is required for device-sourced readings.");
		}

		HealthReading reading = new HealthReading();
		reading.setAstronaut(astronaut);
		reading.setDevice(device);
		reading.setReadingType(request.readingType());
		reading.setValue(request.value());
		reading.setUnit(canonicalUnit(request.readingType()));
		reading.setRecordedAt(request.recordedAt());
		reading.setDataSource(request.dataSource());
		reading.setSourceEventId(
				request.sourceEventId() == null ? null : request.sourceEventId().trim());
		return reading;
	}

	private void validateReading(CreateHealthReadingRequest request) {
		if (request == null || request.astronautId() == null || request.readingType() == null
				|| request.value() == null || request.recordedAt() == null || request.dataSource() == null) {
			throw new InvalidRequestException("Required reading fields are missing.");
		}
		if (request.unit() == null || request.unit().isBlank()) {
			throw new InvalidRequestException("Reading unit is required.");
		}
		String expectedUnit = canonicalUnit(request.readingType());
		if (!expectedUnit.equalsIgnoreCase(request.unit().trim())) {
			throw new InvalidRequestException(
					"Unit for " + request.readingType() + " must be " + expectedUnit + ".");
		}
		BigDecimal value = request.value();
		BigDecimal upperBound = switch (request.readingType()) {
			case HEART_RATE, RESPIRATION_RATE, BLOOD_PRESSURE_SYSTOLIC,
					BLOOD_PRESSURE_DIASTOLIC -> new BigDecimal("1000");
			case SPO2 -> new BigDecimal("100");
			case BODY_TEMPERATURE -> new BigDecimal("100");
			case SLEEP_DURATION, ACTIVITY_DURATION -> new BigDecimal("1440");
			case ACTIVITY_STEPS -> new BigDecimal("10000000");
		};
		boolean mayBeZero = request.readingType() == ReadingType.SPO2
				|| request.readingType() == ReadingType.SLEEP_DURATION
				|| request.readingType() == ReadingType.ACTIVITY_STEPS
				|| request.readingType() == ReadingType.ACTIVITY_DURATION;
		if (value.compareTo(BigDecimal.ZERO) < 0
				|| (!mayBeZero && value.compareTo(BigDecimal.ZERO) == 0)
				|| value.compareTo(upperBound) > 0) {
			throw new InvalidRequestException("Reading value is outside the supported data range.");
		}
		if (request.dataSource() != HealthDataSource.MANUAL
				&& (request.sourceEventId() == null || request.sourceEventId().trim().isEmpty())) {
			throw new InvalidRequestException("Source event ID is required for device and imported readings.");
		}
		if (request.sourceEventId() != null && request.sourceEventId().trim().isEmpty()) {
			throw new InvalidRequestException("Source event ID cannot be blank.");
		}
		if (request.recordedAt().isAfter(Instant.now(clock).plus(ALLOWED_FUTURE_SKEW))) {
			throw new InvalidRequestException("Recorded timestamp is too far in the future.");
		}
	}

	private String canonicalUnit(ReadingType readingType) {
		return switch (readingType) {
			case HEART_RATE -> "bpm";
			case SPO2 -> "%";
			case BODY_TEMPERATURE -> "Cel";
			case RESPIRATION_RATE -> "breaths/min";
			case BLOOD_PRESSURE_SYSTOLIC, BLOOD_PRESSURE_DIASTOLIC -> "mmHg";
			case SLEEP_DURATION, ACTIVITY_DURATION -> "min";
			case ACTIVITY_STEPS -> "count";
		};
	}

	private void validateRange(Instant fromTime, Instant toTime) {
		if (fromTime != null && toTime != null && !fromTime.isBefore(toTime)) {
			throw new InvalidRequestException("The start timestamp must be before the end timestamp.");
		}
	}

	private void requireAstronautExists(Long astronautId) {
		if (!astronautRepository.existsById(astronautId)) {
			throw new ResourceNotFoundException("Astronaut not found with ID: " + astronautId);
		}
	}
}
