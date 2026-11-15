package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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

import com.astrahealth.backend.dto.request.WellbeingReportRequest;
import com.astrahealth.backend.dto.request.WellbeingReviewRequest;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.entity.WellbeingCategory;
import com.astrahealth.backend.entity.WellbeingReport;
import com.astrahealth.backend.entity.WellbeingReportStatus;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;
import com.astrahealth.backend.repository.WellbeingAudioRecordingRepository;
import com.astrahealth.backend.repository.WellbeingReportRepository;

@ExtendWith(MockitoExtension.class)
class WellbeingReportServiceTests {

	@Mock
	private WellbeingReportRepository reportRepository;
	@Mock
	private WellbeingAudioRecordingRepository audioRepository;
	@Mock
	private UserAccountRepository userAccountRepository;
	@Mock
	private MedicalAssignmentRepository assignmentRepository;
	@Mock
	private PrivateAudioStorage audioStorage;
	@Mock
	private MedicalObservationService observationService;
	@Mock
	private NotificationService notificationService;

	private WellbeingReportService reportService;

	@BeforeEach
	void setUp() {
		reportService = new WellbeingReportService(
				reportRepository,
				audioRepository,
				userAccountRepository,
				assignmentRepository,
				audioStorage,
				observationService,
				notificationService,
				Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneOffset.UTC));
	}

	@Test
	void astronautCanSubmitWrittenReportAndAssignedOfficersAreNotified() {
		UserAccount astronautAccount = astronautAccount(3L, 7L);
		when(userAccountRepository.findById(3L)).thenReturn(Optional.of(astronautAccount));
		when(reportRepository.saveAndFlush(any(WellbeingReport.class))).thenAnswer(invocation -> {
			WellbeingReport report = invocation.getArgument(0);
			setReportId(report, 11L);
			return report;
		});
		var response = reportService.submit(
				new WellbeingReportRequest(WellbeingCategory.TIRED, "I need a rest.", null),
				3L);

		assertEquals(11L, response.id());
		assertEquals(WellbeingReportStatus.SUBMITTED, response.status());
		assertEquals("I need a rest.", response.reportText());
		verify(notificationService).notifyMedicalOfficers(
				org.mockito.ArgumentMatchers.eq(7L),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.eq("wellbeing_report"),
				org.mockito.ArgumentMatchers.eq(11L));
	}

	@Test
	void paginatedReportResponsesBatchLoadAudioMetadata() {
		UserAccount astronautAccount = astronautAccount(3L, 7L);
		var first = report(11L, 7L, 3L, WellbeingReportStatus.SUBMITTED);
		var second = report(12L, 7L, 3L, WellbeingReportStatus.SUBMITTED);
		var pageable = PageRequest.of(0, 20);
		when(userAccountRepository.findById(3L)).thenReturn(Optional.of(astronautAccount));
		when(reportRepository.findAllBySubmitterIdOrderBySubmittedAtDesc(3L, pageable))
				.thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));
		when(audioRepository.findAllByReportIdIn(List.of(11L, 12L))).thenReturn(List.of());

		var page = reportService.myReports(3L, pageable);

		assertEquals(2, page.getTotalElements());
		assertFalse(page.getContent().get(0).hasAudio());
		verify(audioRepository).findAllByReportIdIn(List.of(11L, 12L));
		verify(audioRepository, never()).findByReportId(anyLong());
	}

	@Test
	void reportMustContainWrittenTextOrAudio() {
		when(userAccountRepository.findById(3L)).thenReturn(Optional.of(astronautAccount(3L, 7L)));

		assertThrows(
				InvalidRequestException.class,
				() -> reportService.submit(
						new WellbeingReportRequest(WellbeingCategory.OTHER, "  ", null),
						3L));

		verify(reportRepository, never()).saveAndFlush(any(WellbeingReport.class));
	}

	@Test
	void assignedMedicalOfficerCanReviewAndCreatePrivateObservation() {
		WellbeingReport report = report(11L, 7L, 3L, WellbeingReportStatus.SUBMITTED);
		UserAccount officer = user(8L, UserRole.MEDICAL_OFFICER);
		when(reportRepository.findById(11L)).thenReturn(Optional.of(report));
		when(userAccountRepository.findById(8L)).thenReturn(Optional.of(officer));
		when(assignmentRepository.existsByMedicalOfficerIdAndAstronautId(8L, 7L)).thenReturn(true);
		when(reportRepository.saveAndFlush(report)).thenReturn(report);
		when(audioRepository.findByReportId(11L)).thenReturn(Optional.empty());

		var response = reportService.review(
				11L,
				new WellbeingReviewRequest(
						WellbeingReportStatus.UNDER_REVIEW,
						"Review initiated.",
						"Private clinical observation.",
						null,
						null),
				8L);

		assertEquals(WellbeingReportStatus.UNDER_REVIEW, response.status());
		verify(observationService).create(any(), org.mockito.ArgumentMatchers.eq(8L));
		verify(notificationService).notifyUser(
				org.mockito.ArgumentMatchers.eq(3L),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.eq("wellbeing_report"),
				org.mockito.ArgumentMatchers.eq(11L));
	}

	@Test
	void unassignedMedicalOfficerCannotReadOrReviewReport() {
		WellbeingReport report = report(11L, 7L, 3L, WellbeingReportStatus.SUBMITTED);
		when(reportRepository.findById(11L)).thenReturn(Optional.of(report));
		when(userAccountRepository.findById(8L))
				.thenReturn(Optional.of(user(8L, UserRole.MEDICAL_OFFICER)));
		when(assignmentRepository.existsByMedicalOfficerIdAndAstronautId(8L, 7L)).thenReturn(false);

		assertThrows(AccessDeniedException.class, () -> reportService.get(11L, 8L));
		assertThrows(
				AccessDeniedException.class,
				() -> reportService.review(
						11L,
						new WellbeingReviewRequest(WellbeingReportStatus.UNDER_REVIEW, null, null, null, null),
						8L));

		verify(reportRepository, never()).saveAndFlush(report);
	}

	@Test
	void unassignedOfficerIsDeniedBeforeAudioStorageIsRead() {
		WellbeingReport report = report(11L, 7L, 3L, WellbeingReportStatus.SUBMITTED);
		when(reportRepository.findById(11L)).thenReturn(Optional.of(report));
		when(userAccountRepository.findById(8L))
				.thenReturn(Optional.of(user(8L, UserRole.MEDICAL_OFFICER)));
		when(assignmentRepository.existsByMedicalOfficerIdAndAstronautId(8L, 7L)).thenReturn(false);

		assertThrows(AccessDeniedException.class, () -> reportService.downloadReportAudio(11L, 8L));

		verify(audioRepository, never()).findByReportId(11L);
		verify(audioStorage, never()).read(anyString());
	}

	private UserAccount astronautAccount(Long userId, Long astronautId) {
		UserAccount account = user(userId, UserRole.ASTRONAUT);
		Astronaut astronaut = new Astronaut();
		astronaut.setId(astronautId);
		account.setAstronautProfile(astronaut);
		return account;
	}

	private UserAccount user(Long id, UserRole role) {
		UserAccount account = new UserAccount();
		account.setId(id);
		account.setRoles(EnumSet.of(role));
		return account;
	}

	private WellbeingReport report(
			Long reportId,
			Long astronautId,
			Long submitterId,
			WellbeingReportStatus status) {
		WellbeingReport report = new WellbeingReport();
		setReportId(report, reportId);
		Astronaut astronaut = new Astronaut();
		astronaut.setId(astronautId);
		report.setAstronaut(astronaut);
		report.setSubmitter(user(submitterId, UserRole.ASTRONAUT));
		report.setStatus(status);
		return report;
	}

	private void setReportId(WellbeingReport report, Long id) {
		try {
			var field = WellbeingReport.class.getDeclaredField("id");
			field.setAccessible(true);
			field.set(report, id);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}
}
