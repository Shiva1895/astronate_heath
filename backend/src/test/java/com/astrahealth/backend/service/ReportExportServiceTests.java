package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import com.astrahealth.backend.dto.response.MissionOperationalSummary;
import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AlertStatus;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.Mission;
import com.astrahealth.backend.entity.MissionStatus;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.repository.AlertRepository;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.MedicalFollowUpActionRepository;
import com.astrahealth.backend.repository.MissionRepository;
import com.astrahealth.backend.repository.UserAccountRepository;
import com.astrahealth.backend.repository.WellbeingAudioRecordingRepository;
import com.astrahealth.backend.repository.WellbeingReportRepository;
import com.astrahealth.backend.security.HealthAccessService;

@ExtendWith(MockitoExtension.class)
class ReportExportServiceTests {

	@Mock private HealthReadingRepository readingRepository;
	@Mock private AlertRepository alertRepository;
	@Mock private MedicalFollowUpActionRepository followUpRepository;
	@Mock private WellbeingReportRepository wellbeingRepository;
	@Mock private WellbeingAudioRecordingRepository audioRepository;
	@Mock private AstronautRepository astronautRepository;
	@Mock private MissionRepository missionRepository;
	@Mock private UserAccountRepository userAccountRepository;
	@Mock private MedicalAssignmentRepository assignmentRepository;
	@Mock private HealthAccessService healthAccessService;
	@Mock private HealthDashboardService dashboardService;

	private ReportExportService exportService;

	@BeforeEach
	void setUp() {
		exportService = new ReportExportService(
				readingRepository,
				alertRepository,
				followUpRepository,
				wellbeingRepository,
				audioRepository,
				astronautRepository,
				missionRepository,
				userAccountRepository,
				assignmentRepository,
				healthAccessService,
				dashboardService);
	}

	@Test
	void csvEscapesCellsAndPreventsSpreadsheetFormulaExecution() {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		Alert alert = new Alert();
		alert.setAstronaut(astronaut);
		alert.setTitle("Example");
		alert.setReason("=1+1");
		alert.setPriority(AlertPriority.LOW);
		alert.setStatus(AlertStatus.OPEN);
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(alertRepository.search(
				org.mockito.ArgumentMatchers.eq(7L),
				org.mockito.ArgumentMatchers.isNull(),
				org.mockito.ArgumentMatchers.isNull(),
				org.mockito.ArgumentMatchers.any()))
				.thenReturn(new PageImpl<>(List.of(alert), PageRequest.of(0, 5001), 1));

		var file = exportService.alerts(7L, ReportFormat.CSV, 4L);
		String csv = new String(file.contents(), java.nio.charset.StandardCharsets.UTF_8);

		assertEquals("text/csv; charset=UTF-8", file.contentType());
		assertTrue(csv.contains("\"'=1+1\""));
		assertTrue(csv.contains("\"Example\""));
	}

	@Test
	void missionReportCanBeGeneratedAsPdfForMissionControl() {
		UserAccount missionControl = user(4L, UserRole.MISSION_CONTROL);
		when(userAccountRepository.findById(4L)).thenReturn(Optional.of(missionControl));
		when(missionRepository.findByIdAndArchivedAtIsNull(9L)).thenReturn(Optional.of(new Mission()));
		when(dashboardService.missionSummary(9L)).thenReturn(new MissionOperationalSummary(
				9L, "M-9", "Mission Nine", MissionStatus.ACTIVE, 2, 1, 0, Instant.parse("2026-01-01T00:00:00Z")));

		var file = exportService.mission(9L, ReportFormat.PDF, 4L);

		assertEquals("application/pdf", file.contentType());
		assertTrue(new String(file.contents(), 0, 4, java.nio.charset.StandardCharsets.US_ASCII)
				.startsWith("%PDF"));
	}

	@Test
	void missionControlCannotExportRawAlertReportsWithoutAstronautScope() {
		when(userAccountRepository.findById(4L))
				.thenReturn(Optional.of(user(4L, UserRole.MISSION_CONTROL)));

		assertThrows(
				AccessDeniedException.class,
				() -> exportService.alerts(null, ReportFormat.CSV, 4L));
	}

	private UserAccount user(Long id, UserRole role) {
		UserAccount user = new UserAccount();
		user.setId(id);
		user.setRoles(EnumSet.of(role));
		return user;
	}
}
