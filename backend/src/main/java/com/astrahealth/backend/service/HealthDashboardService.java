package com.astrahealth.backend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.response.AstronautDashboardSummary;
import com.astrahealth.backend.dto.response.HealthReadingResponse;
import com.astrahealth.backend.dto.response.MissionOperationalSummary;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.DeviceStatus;
import com.astrahealth.backend.entity.MedicalAssignment;
import com.astrahealth.backend.entity.Mission;
import com.astrahealth.backend.entity.MissionStatus;
import com.astrahealth.backend.entity.AlertStatus;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AlertRepository;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.DeviceRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.MissionRepository;
import com.astrahealth.backend.repository.UserAccountRepository;
import com.astrahealth.backend.repository.projection.ActiveAlertCount;
import com.astrahealth.backend.repository.projection.AstronautDeviceStatusCount;

@Service
@Transactional(readOnly = true)
public class HealthDashboardService {

	private static final Set<AlertStatus> ACTIVE_STATUSES =
			Set.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED, AlertStatus.ESCALATED);

	private final AstronautRepository astronautRepository;
	private final MedicalAssignmentRepository assignmentRepository;
	private final MissionRepository missionRepository;
	private final HealthReadingRepository readingRepository;
	private final AlertRepository alertRepository;
	private final DeviceRepository deviceRepository;
	private final UserAccountRepository userAccountRepository;

	public HealthDashboardService(
			AstronautRepository astronautRepository,
			MedicalAssignmentRepository assignmentRepository,
			MissionRepository missionRepository,
			HealthReadingRepository readingRepository,
			AlertRepository alertRepository,
			DeviceRepository deviceRepository,
			UserAccountRepository userAccountRepository) {
		this.astronautRepository = astronautRepository;
		this.assignmentRepository = assignmentRepository;
		this.missionRepository = missionRepository;
		this.readingRepository = readingRepository;
		this.alertRepository = alertRepository;
		this.deviceRepository = deviceRepository;
		this.userAccountRepository = userAccountRepository;
	}

	public Page<AstronautDashboardSummary> astronautSummaries(Long userId, Pageable pageable) {
		UserAccount account = userAccountRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User account not found with ID: " + userId));
		if (account.getRoles().contains(UserRole.ADMIN)) {
			return allAstronauts(pageable);
		}
		if (!account.getRoles().contains(UserRole.MEDICAL_OFFICER)) {
			throw new org.springframework.security.access.AccessDeniedException(
					"Only administrators and medical officers can view astronaut summaries.");
		}
		Page<MedicalAssignment> assignments = assignmentRepository.findAllByMedicalOfficerId(userId, pageable);
		List<Astronaut> astronauts = assignments.map(MedicalAssignment::getAstronaut).toList();
		SummaryData data = loadSummaryData(astronauts);
		return assignments.map(assignment -> toAstronautSummary(assignment.getAstronaut(), data));
	}

	public Page<AstronautDashboardSummary> allAstronauts(Pageable pageable) {
		Page<Astronaut> astronauts = astronautRepository.findAll(pageable);
		SummaryData data = loadSummaryData(astronauts.getContent());
		return astronauts.map(astronaut -> toAstronautSummary(astronaut, data));
	}

	public Page<MissionOperationalSummary> missionSummaries(
			MissionStatus status,
			Pageable pageable) {
		Page<Mission> missions = missionRepository.search(null, status, pageable);
		List<Astronaut> astronauts = missions.getContent().stream()
				.flatMap(mission -> mission.getAstronauts().stream())
				.distinct()
				.toList();
		SummaryData data = loadSummaryData(astronauts);
		return missions.map(mission -> toMissionSummary(mission, data));
	}

	public MissionOperationalSummary missionSummary(Long missionId) {
		Mission mission = missionRepository.findByIdAndArchivedAtIsNull(missionId)
				.orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + missionId));
		SummaryData data = loadSummaryData(new ArrayList<>(mission.getAstronauts()));
		return toMissionSummary(mission, data);
	}

	private SummaryData loadSummaryData(Collection<Astronaut> astronauts) {
		List<Long> ids = astronauts.stream().map(Astronaut::getId).distinct().toList();
		if (ids.isEmpty()) {
			return new SummaryData(Map.of(), Map.of(), Map.of());
		}

		Map<Long, List<HealthReadingResponse>> latestReadings = new HashMap<>();
		Map<Long, Instant> latestReadingAt = new HashMap<>();
		readingRepository.findLatestForAstronauts(ids).forEach(reading -> {
			latestReadings.computeIfAbsent(reading.getAstronaut().getId(), ignored -> new ArrayList<>())
					.add(HealthReadingResponse.from(reading));
			latestReadingAt.merge(
					reading.getAstronaut().getId(),
					reading.getRecordedAt(),
					(left, right) -> left.isAfter(right) ? left : right);
		});

		Map<Long, Long> activeAlertCounts = new HashMap<>();
		for (ActiveAlertCount count : alertRepository.countActiveGroupedByAstronaut(ids, ACTIVE_STATUSES)) {
			activeAlertCounts.put(count.getAstronautId(), count.getAlertCount());
		}

		Map<Long, Long> offlineDeviceCounts = new HashMap<>();
		for (AstronautDeviceStatusCount count : deviceRepository.countGroupedByAstronautAndStatus(ids)) {
			if (count.getDeviceStatus() == DeviceStatus.OFFLINE) {
				offlineDeviceCounts.put(count.getAstronautId(), count.getDeviceCount());
			}
		}
		return new SummaryData(latestReadings, latestReadingAt, activeAlertCounts, offlineDeviceCounts);
	}

	private AstronautDashboardSummary toAstronautSummary(Astronaut astronaut, SummaryData data) {
		return new AstronautDashboardSummary(
				astronaut.getId(),
				astronaut.getAstronautCode(),
				astronaut.getFullName(),
				astronaut.getStatus(),
				data.latestReadings().getOrDefault(astronaut.getId(), List.of()),
				data.activeAlertCounts().getOrDefault(astronaut.getId(), 0L),
				data.latestReadingAt().get(astronaut.getId()));
	}

	private MissionOperationalSummary toMissionSummary(Mission mission, SummaryData data) {
		long activeAlerts = 0;
		long offlineDevices = 0;
		Instant latestReading = null;
		for (Astronaut astronaut : mission.getAstronauts()) {
			activeAlerts += data.activeAlertCounts().getOrDefault(astronaut.getId(), 0L);
			offlineDevices += data.offlineDeviceCounts().getOrDefault(astronaut.getId(), 0L);
			Instant astronautLatest = data.latestReadingAt().get(astronaut.getId());
			if (astronautLatest != null && (latestReading == null || astronautLatest.isAfter(latestReading))) {
				latestReading = astronautLatest;
			}
		}
		return new MissionOperationalSummary(
				mission.getId(),
				mission.getMissionCode(),
				mission.getMissionName(),
				mission.getStatus(),
				mission.getAstronauts().size(),
				activeAlerts,
				offlineDevices,
				latestReading);
	}

	private record SummaryData(
			Map<Long, List<HealthReadingResponse>> latestReadings,
			Map<Long, Instant> latestReadingAt,
			Map<Long, Long> activeAlertCounts,
			Map<Long, Long> offlineDeviceCounts) {
		SummaryData(
				Map<Long, List<HealthReadingResponse>> latestReadings,
				Map<Long, Instant> latestReadingAt,
				Map<Long, Long> activeAlertCounts) {
			this(latestReadings, latestReadingAt, activeAlertCounts, Map.of());
		}
	}
}
