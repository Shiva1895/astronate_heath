package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AlertStatus;
import com.astrahealth.backend.entity.Device;
import com.astrahealth.backend.entity.HealthDataSource;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.DeviceStatus;
import com.astrahealth.backend.entity.Mission;
import com.astrahealth.backend.entity.MissionStatus;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.repository.AlertRepository;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.DeviceRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.repository.MissionRepository;
import com.astrahealth.backend.dto.response.MissionOperationalSummary;

@ActiveProfiles("test")
@SpringBootTest(properties = "astra.security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@Transactional
class HealthReadingPersistenceTests {

	@Autowired
	private AstronautRepository astronautRepository;

	@Autowired
	private HealthReadingRepository readingRepository;

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private MissionRepository missionRepository;

	@Autowired
	private HealthSummaryService summaryService;

	@Autowired
	private HealthDashboardService dashboardService;

	@Test
	void databaseReturnsDailyAggregatesAndLatestValuesWithoutLoadingRawHistory() {
		Astronaut astronaut = new Astronaut();
		astronaut.setAstronautCode("AGG-" + java.util.UUID.randomUUID());
		astronaut.setFullName("Aggregate Test");
		astronaut.setStatus("ACTIVE");
		astronaut = astronautRepository.saveAndFlush(astronaut);

		save(astronaut, "70", "2026-11-01T10:00:00Z");
		save(astronaut, "80", "2026-11-01T12:00:00Z");
		save(astronaut, "90", "2026-11-02T10:00:00Z");

		List<com.astrahealth.backend.dto.response.HealthSummaryBucket> summaries = summaryService.daily(
				astronaut.getId(),
				Instant.parse("2026-11-01T00:00:00Z"),
				Instant.parse("2026-11-03T00:00:00Z"),
				ReadingType.HEART_RATE);

		assertEquals(2, summaries.size());
		assertEquals(new BigDecimal("70.000000"), summaries.get(0).minimumValue());
		assertEquals(new BigDecimal("80.000000"), summaries.get(0).maximumValue());
		assertEquals(2L, summaries.get(0).readingCount());
		assertEquals(0, summaries.get(0).averageValue().compareTo(new BigDecimal("75")));
		assertEquals(new BigDecimal("80.000000"), summaries.get(0).latestValue());
		assertTrue(summaries.get(0).latestRecordedAt().isAfter(Instant.parse("2026-11-01T11:00:00Z")));
		var comparison = summaryService.compare(
				astronaut.getId(),
				ReadingType.HEART_RATE,
				Instant.parse("2026-11-01T00:00:00Z"),
				Instant.parse("2026-11-02T00:00:00Z"),
				Instant.parse("2026-11-02T00:00:00Z"),
				Instant.parse("2026-11-03T00:00:00Z"));
		assertEquals(0, comparison.averageDifference().compareTo(new BigDecimal("15")));
		assertEquals(0, comparison.averagePercentChange().compareTo(new BigDecimal("20.0000")));
	}

	@Test
	void missionDashboardUsesOperationalCountsWithoutReturningPrivateReadingValues() {
		Astronaut astronaut = new Astronaut();
		astronaut.setAstronautCode("OPS-" + java.util.UUID.randomUUID());
		astronaut.setFullName("Operational Test");
		astronaut.setStatus("ACTIVE");
		astronaut = astronautRepository.saveAndFlush(astronaut);
		save(astronaut, "75", "2026-11-01T12:00:00Z");

		Device device = new Device();
		device.setDeviceName("Operational sensor");
		device.setSerialNumber("OPS-" + java.util.UUID.randomUUID());
		device.setDeviceType("VITALS_SENSOR");
		device.setStatus(DeviceStatus.OFFLINE);
		device.setAssignedAstronaut(astronaut);
		deviceRepository.saveAndFlush(device);

		Alert alert = new Alert();
		alert.setAstronaut(astronaut);
		alert.setTitle("Operational alert");
		alert.setDescription("Status count only");
		alert.setReason("Configured rule triggered.");
		alert.setPriority(AlertPriority.HIGH);
		alert.setStatus(AlertStatus.OPEN);
		alertRepository.saveAndFlush(alert);

		Mission mission = new Mission();
		mission.setMissionCode("OPS-" + java.util.UUID.randomUUID());
		mission.setMissionName("Operational Summary Test");
		mission.setStartDate(java.time.LocalDate.parse("2026-01-01"));
		mission.setEndDate(java.time.LocalDate.parse("2027-01-01"));
		mission.setStatus(MissionStatus.ACTIVE);
		mission.getAstronauts().add(astronaut);
		mission = missionRepository.saveAndFlush(mission);

		MissionOperationalSummary summary = dashboardService.missionSummary(mission.getId());

		assertEquals(1, summary.astronautCount());
		assertEquals(1L, summary.activeAlertCount());
		assertEquals(1L, summary.offlineDeviceCount());
		assertEquals(Instant.parse("2026-11-01T12:00:00Z"), summary.latestReadingAt());
	}

	private void save(Astronaut astronaut, String value, String recordedAt) {
		HealthReading reading = new HealthReading();
		reading.setAstronaut(astronaut);
		reading.setReadingType(ReadingType.HEART_RATE);
		reading.setValue(new BigDecimal(value));
		reading.setUnit("bpm");
		reading.setRecordedAt(Instant.parse(recordedAt));
		reading.setDataSource(HealthDataSource.MANUAL);
		readingRepository.saveAndFlush(reading);
	}
}
