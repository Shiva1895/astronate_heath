package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.astrahealth.backend.dto.request.CreateHealthReadingRequest;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.HealthDataSource;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.DeviceRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.security.HealthAccessService;

@ExtendWith(MockitoExtension.class)
class HealthReadingServiceTests {

	@Mock
	private HealthReadingRepository readingRepository;
	@Mock
	private AstronautRepository astronautRepository;
	@Mock
	private DeviceRepository deviceRepository;
	@Mock
	private HealthAccessService healthAccessService;
	@Mock
	private AnomalyDetectionService anomalyDetectionService;
	@Mock
	private Clock clock;

	@InjectMocks
	private HealthReadingService readingService;

	@BeforeEach
	void setUp() {
		lenient().when(clock.instant()).thenReturn(Instant.parse("2026-11-15T12:00:00Z"));
	}

	@Test
	void savesAuthorizedReadingAndRunsConfiguredAnomalyRules() {
		Astronaut astronaut = astronaut(7L);
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(readingRepository.saveAndFlush(any(HealthReading.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var response = readingService.create(request(7L, "bpm", "78", null, null), 11L);

		assertEquals(7L, response.astronautId());
		assertEquals(ReadingType.HEART_RATE, response.readingType());
		verify(healthAccessService).requireAccess(7L, 11L);
		verify(anomalyDetectionService).analyze(any(HealthReading.class));
	}

	@Test
	void rejectsInvalidMeasurementUnitBeforePersistence() {
		assertThrows(
				InvalidRequestException.class,
				() -> readingService.create(request(7L, "seconds", "78", null, null), 11L));

		verify(readingRepository, never()).saveAndFlush(any(HealthReading.class));
	}

	@Test
	void rejectsDuplicateDeviceEventId() {
		Astronaut astronaut = astronaut(7L);
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(readingRepository.existsBySourceEventId("evt-7")).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> readingService.create(
						request(7L, "bpm", "78", HealthDataSource.IMPORT, "evt-7"),
						11L));

		verify(readingRepository, never()).saveAndFlush(any(HealthReading.class));
	}

	@Test
	void rejectsReadingSubmissionWithoutAstronautAccess() {
		Astronaut astronaut = astronaut(7L);
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		doThrow(new AccessDeniedException("forbidden"))
				.when(healthAccessService).requireAccess(7L, 11L);

		assertThrows(
				AccessDeniedException.class,
				() -> readingService.create(request(7L, "bpm", "78", null, null), 11L));

		verify(readingRepository, never()).saveAndFlush(any(HealthReading.class));
	}

	@Test
	void historyPassesMeasurementAndTimeFiltersToTheRepository() {
		PageRequest page = PageRequest.of(0, 20);
		when(astronautRepository.existsById(7L)).thenReturn(true);
		when(readingRepository.search(7L, ReadingType.HEART_RATE, Instant.EPOCH, Instant.MAX, page))
				.thenReturn(Page.empty(page));

		readingService.history(7L, ReadingType.HEART_RATE, Instant.EPOCH, Instant.MAX, 11L, page);

		verify(healthAccessService).requireAccess(7L, 11L);
		verify(readingRepository).search(7L, ReadingType.HEART_RATE, Instant.EPOCH, Instant.MAX, page);
	}

	private CreateHealthReadingRequest request(
			Long astronautId,
			String unit,
			String value,
			HealthDataSource dataSource,
			String eventId) {
		return new CreateHealthReadingRequest(
				astronautId,
				null,
				ReadingType.HEART_RATE,
				new BigDecimal(value),
				unit,
				Instant.parse("2026-11-15T10:00:00Z"),
				dataSource == null ? HealthDataSource.MANUAL : dataSource,
				eventId);
	}

	private Astronaut astronaut(Long id) {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(id);
		astronaut.setAstronautCode("AST-" + id);
		astronaut.setFullName("Test Astronaut");
		astronaut.setStatus("ACTIVE");
		return astronaut;
	}
}
