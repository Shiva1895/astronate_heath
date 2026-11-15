package com.astrahealth.backend.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.astrahealth.backend.dto.request.MedicalObservationRequest;
import com.astrahealth.backend.dto.request.WellbeingReportRequest;
import com.astrahealth.backend.dto.request.WellbeingReviewRequest;
import com.astrahealth.backend.dto.response.WellbeingAudioResponse;
import com.astrahealth.backend.dto.response.WellbeingReportResponse;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.NotificationType;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.entity.WellbeingAudioRecording;
import com.astrahealth.backend.entity.WellbeingCategory;
import com.astrahealth.backend.entity.WellbeingReport;
import com.astrahealth.backend.entity.WellbeingReportStatus;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;
import com.astrahealth.backend.repository.WellbeingAudioRecordingRepository;
import com.astrahealth.backend.repository.WellbeingReportRepository;

@Service
@Transactional(readOnly = true)
public class WellbeingReportService {

	private final WellbeingReportRepository reportRepository;
	private final WellbeingAudioRecordingRepository audioRepository;
	private final UserAccountRepository userAccountRepository;
	private final MedicalAssignmentRepository assignmentRepository;
	private final PrivateAudioStorage audioStorage;
	private final MedicalObservationService observationService;
	private final NotificationService notificationService;
	private final Clock clock;

	public WellbeingReportService(
			WellbeingReportRepository reportRepository,
			WellbeingAudioRecordingRepository audioRepository,
			UserAccountRepository userAccountRepository,
			MedicalAssignmentRepository assignmentRepository,
			PrivateAudioStorage audioStorage,
			MedicalObservationService observationService,
			NotificationService notificationService,
			Clock clock) {
		this.reportRepository = reportRepository;
		this.audioRepository = audioRepository;
		this.userAccountRepository = userAccountRepository;
		this.assignmentRepository = assignmentRepository;
		this.audioStorage = audioStorage;
		this.observationService = observationService;
		this.notificationService = notificationService;
		this.clock = clock;
	}

	@Transactional
	public WellbeingAudioResponse uploadDraft(MultipartFile upload, Long userId) {
		UserAccount owner = requireAstronaut(userId);
		PrivateAudioStorage.StoredAudio stored = audioStorage.store(upload);
		WellbeingAudioRecording recording = new WellbeingAudioRecording();
		recording.setOwner(owner);
		recording.setStorageKey(stored.storageKey());
		recording.setMediaType(stored.mediaType());
		recording.setByteSize(stored.byteSize());
		recording.setSha256(stored.sha256());
		try {
			return WellbeingAudioResponse.from(audioRepository.saveAndFlush(recording));
		} catch (DataAccessException exception) {
			audioStorage.delete(stored.storageKey());
			throw exception;
		}
	}

	@Transactional
	public void deleteDraft(Long audioId, Long userId) {
		requireAstronaut(userId);
		WellbeingAudioRecording recording = audioRepository
				.findByIdAndOwnerIdAndReportIsNull(audioId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Unsubmitted audio recording not found."));
		audioStorage.delete(recording.getStorageKey());
		audioRepository.delete(recording);
	}

	public PrivateAudio downloadDraft(Long audioId, Long userId) {
		WellbeingAudioRecording recording = audioRepository.findByIdAndOwnerId(audioId, userId)
				.filter(candidate -> candidate.getReport() == null)
				.orElseThrow(() -> new ResourceNotFoundException("Unsubmitted audio recording not found."));
		requireAstronaut(userId);
		return new PrivateAudio(recording.getMediaType(), audioStorage.read(recording.getStorageKey()));
	}

	@Transactional
	public WellbeingReportResponse submit(WellbeingReportRequest request, Long userId) {
		if (request == null || request.category() == null) {
			throw new InvalidRequestException("Feeling category is required.");
		}
		UserAccount submitter = requireAstronaut(userId);
		String text = normalizeText(request.reportText());
		if (text == null && request.audioRecordingId() == null) {
			throw new InvalidRequestException("A written report or audio recording is required.");
		}

		WellbeingReport report = new WellbeingReport();
		report.setAstronaut(submitter.getAstronautProfile());
		report.setSubmitter(submitter);
		report.setCategory(request.category());
		report.setReportText(text);
		report.setStatus(WellbeingReportStatus.SUBMITTED);
		report = reportRepository.saveAndFlush(report);

		Long audioId = null;
		if (request.audioRecordingId() != null) {
			WellbeingAudioRecording recording = audioRepository
					.findByIdAndOwnerIdAndReportIsNull(request.audioRecordingId(), userId)
					.orElseThrow(() -> new ResourceNotFoundException(
							"Unsubmitted audio recording not found."));
			recording.setReport(report);
			audioRepository.save(recording);
			audioId = recording.getId();
		}

		notificationService.notifyMedicalOfficers(
				report.getAstronaut().getId(),
				NotificationType.WELLBEING_REPORT,
				"New well-being report",
				"A report assigned to you is ready for review.",
				"wellbeing_report",
				report.getId());
		return WellbeingReportResponse.from(report, audioId);
	}

	@Transactional
	public WellbeingReportResponse edit(
			Long reportId,
			WellbeingReportRequest request,
			Long userId) {
		requireAstronaut(userId);
		WellbeingReport report = getReport(reportId);
		if (!report.getSubmitter().getId().equals(userId)) {
			throw new AccessDeniedException("Only the report submitter can edit this report.");
		}
		if (report.getStatus() != WellbeingReportStatus.SUBMITTED) {
			throw new InvalidRequestException("A report can only be edited before review begins.");
		}
		if (request == null || request.category() == null) {
			throw new InvalidRequestException("Feeling category is required.");
		}
		String text = normalizeText(request.reportText());
		boolean hasAudio = audioRepository.findByReportId(reportId).isPresent();
		WellbeingAudioRecording newRecording = null;
		if (request.audioRecordingId() != null) {
			if (hasAudio) {
				throw new InvalidRequestException("A report can contain only one audio recording.");
			}
			newRecording = audioRepository
					.findByIdAndOwnerIdAndReportIsNull(request.audioRecordingId(), userId)
					.orElseThrow(() -> new ResourceNotFoundException(
							"Unsubmitted audio recording not found."));
			hasAudio = true;
		}
		if (text == null && !hasAudio) {
			throw new InvalidRequestException("A written report or audio recording is required.");
		}
		report.setCategory(request.category());
		report.setReportText(text);
		report = reportRepository.saveAndFlush(report);
		if (newRecording != null) {
			newRecording.setReport(report);
			audioRepository.save(newRecording);
		}
		return WellbeingReportResponse.from(report, audioRepository.findByReportId(reportId)
				.map(WellbeingAudioRecording::getId).orElse(null));
	}

	public Page<WellbeingReportResponse> myReports(Long userId, Pageable pageable) {
		requireAstronaut(userId);
		return responsePage(reportRepository.findAllBySubmitterIdOrderBySubmittedAtDesc(userId, pageable));
	}

	public Page<WellbeingReportResponse> inbox(
			Long userId,
			WellbeingReportStatus status,
			Pageable pageable) {
		UserAccount actor = requireMedicalManager(userId, null);
		Page<WellbeingReport> reports;
		if (actor.getRoles().contains(UserRole.ADMIN)) {
			reports = status == null
					? reportRepository.findAllByOrderBySubmittedAtAsc(pageable)
					: reportRepository.findAllByStatusOrderBySubmittedAtAsc(status, pageable);
		} else {
			List<Long> astronautIds = assignmentRepository.findAllByMedicalOfficerId(userId).stream()
					.map(assignment -> assignment.getAstronaut().getId())
					.distinct()
					.toList();
			if (astronautIds.isEmpty()) {
				return Page.empty(pageable);
			}
			reports = status == null
					? reportRepository.findAllByAstronautIdInOrderBySubmittedAtAsc(astronautIds, pageable)
					: reportRepository.findAllByAstronautIdInAndStatusOrderBySubmittedAtAsc(
							astronautIds, status, pageable);
		}
		return responsePage(reports);
	}

	public WellbeingReportResponse get(Long reportId, Long userId) {
		WellbeingReport report = getReport(reportId);
		requireReportAccess(report, userId);
		return response(report);
	}

	@Transactional
	public WellbeingReportResponse review(
			Long reportId,
			WellbeingReviewRequest request,
			Long userId) {
		if (request == null || request.status() == null) {
			throw new InvalidRequestException("A report status is required.");
		}
		WellbeingReport report = getReport(reportId);
		UserAccount reviewer = requireMedicalManager(userId, report.getAstronaut().getId());
		validateTransition(report.getStatus(), request.status());
		String reviewNotes = normalizeText(request.reviewNotes());

		if (request.status() == WellbeingReportStatus.FOLLOW_UP_REQUIRED) {
			if (request.followUpAssigneeId() == null || request.followUpDueDate() == null) {
				throw new InvalidRequestException(
						"A follow-up assignee and due date are required for follow-up reports.");
			}
			if (request.followUpDueDate().isBefore(java.time.LocalDate.now(clock))) {
				throw new InvalidRequestException("Follow-up due date cannot be in the past.");
			}
			UserAccount assignee = requireFollowUpAssignee(
					request.followUpAssigneeId(), report.getAstronaut().getId());
			report.setFollowUpAssignee(assignee);
			report.setFollowUpDueDate(request.followUpDueDate());
		} else if (request.followUpAssigneeId() != null || request.followUpDueDate() != null) {
			throw new InvalidRequestException(
					"Follow-up assignment fields are only valid when status is FOLLOW_UP_REQUIRED.");
		}

		report.setStatus(request.status());
		report.setReviewedBy(reviewer);
		report.setReviewNotes(reviewNotes);
		if (request.status() == WellbeingReportStatus.REVIEWED
				|| request.status() == WellbeingReportStatus.FOLLOW_UP_REQUIRED
				|| request.status() == WellbeingReportStatus.CLOSED) {
			report.setReviewedAt(Instant.now(clock));
		}
		report = reportRepository.saveAndFlush(report);

		if (StringUtils.hasText(request.observationText())) {
			observationService.create(
					new MedicalObservationRequest(
							report.getAstronaut().getId(),
							request.observationText(),
							null,
							null,
							request.followUpDueDate()),
					userId);
		}
		notificationService.notifyUser(
				report.getSubmitter().getId(),
				NotificationType.WELLBEING_REPORT,
				"Well-being report status updated",
				"Your well-being report status has changed.",
				"wellbeing_report",
				report.getId());
		if (report.getFollowUpAssignee() != null) {
			notificationService.notifyUser(
					report.getFollowUpAssignee().getId(),
					NotificationType.FOLLOW_UP,
					"Well-being follow-up assigned",
					"A follow-up task has been assigned to you.",
					"wellbeing_report",
					report.getId());
		}
		return response(report);
	}

	public PrivateAudio downloadReportAudio(Long reportId, Long userId) {
		WellbeingReport report = getReport(reportId);
		requireReportAccess(report, userId);
		WellbeingAudioRecording recording = audioRepository.findByReportId(reportId)
				.orElseThrow(() -> new ResourceNotFoundException("Audio recording not found for this report."));
		return new PrivateAudio(recording.getMediaType(), audioStorage.read(recording.getStorageKey()));
	}

	private WellbeingReportResponse response(WellbeingReport report) {
		Long audioId = audioRepository.findByReportId(report.getId())
				.map(WellbeingAudioRecording::getId)
				.orElse(null);
		return WellbeingReportResponse.from(report, audioId);
	}

	private Page<WellbeingReportResponse> responsePage(Page<WellbeingReport> reports) {
		List<Long> reportIds = reports.getContent().stream().map(WellbeingReport::getId).toList();
		Map<Long, Long> audioIdsByReport = reportIds.isEmpty()
				? Map.of()
				: audioRepository.findAllByReportIdIn(reportIds).stream()
						.collect(Collectors.toMap(
								recording -> recording.getReport().getId(),
								WellbeingAudioRecording::getId));
		return reports.map(report ->
				WellbeingReportResponse.from(report, audioIdsByReport.get(report.getId())));
	}

	private void requireReportAccess(WellbeingReport report, Long userId) {
		UserAccount actor = userAccountRepository.findById(userId)
				.orElseThrow(() -> new AccessDeniedException("The authenticated account cannot access this report."));
		if (actor.getRoles().contains(UserRole.ADMIN)
				|| report.getSubmitter().getId().equals(userId)
				|| (actor.getRoles().contains(UserRole.MEDICAL_OFFICER)
						&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(
								userId, report.getAstronaut().getId()))) {
			return;
		}
		throw new AccessDeniedException("The authenticated account cannot access this report.");
	}

	private UserAccount requireAstronaut(Long userId) {
		UserAccount account = userAccountRepository.findById(userId)
				.orElseThrow(() -> new AccessDeniedException("An active astronaut account is required."));
		if (!account.getRoles().contains(UserRole.ASTRONAUT) || account.getAstronautProfile() == null) {
			throw new AccessDeniedException("An astronaut profile is required to submit a well-being report.");
		}
		return account;
	}

	private UserAccount requireMedicalManager(Long userId, Long astronautId) {
		UserAccount actor = userAccountRepository.findById(userId)
				.orElseThrow(() -> new AccessDeniedException("A medical reviewer account is required."));
		if (actor.getRoles().contains(UserRole.ADMIN)) {
			return actor;
		}
		if (actor.getRoles().contains(UserRole.MEDICAL_OFFICER)
				&& astronautId != null
				&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(userId, astronautId)) {
			return actor;
		}
		if (actor.getRoles().contains(UserRole.MEDICAL_OFFICER) && astronautId == null) {
			return actor;
		}
		throw new AccessDeniedException("Only an administrator or assigned medical officer can review reports.");
	}

	private UserAccount requireFollowUpAssignee(Long assigneeId, Long astronautId) {
		UserAccount assignee = userAccountRepository.findById(assigneeId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Follow-up assignee not found with ID: " + assigneeId));
		if (assignee.getRoles().contains(UserRole.ADMIN)
				|| (assignee.getRoles().contains(UserRole.MEDICAL_OFFICER)
						&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(assigneeId, astronautId))) {
			return assignee;
		}
		throw new InvalidRequestException("The follow-up assignee is not authorized for this astronaut.");
	}

	private WellbeingReport getReport(Long id) {
		return reportRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Well-being report not found with ID: " + id));
	}

	private String normalizeText(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private void validateTransition(WellbeingReportStatus current, WellbeingReportStatus target) {
		boolean allowed = switch (current) {
			case SUBMITTED -> target == WellbeingReportStatus.UNDER_REVIEW;
			case UNDER_REVIEW -> target == WellbeingReportStatus.REVIEWED
					|| target == WellbeingReportStatus.FOLLOW_UP_REQUIRED;
			case REVIEWED -> target == WellbeingReportStatus.FOLLOW_UP_REQUIRED
					|| target == WellbeingReportStatus.CLOSED;
			case FOLLOW_UP_REQUIRED -> target == WellbeingReportStatus.REVIEWED
					|| target == WellbeingReportStatus.CLOSED;
			case CLOSED -> false;
		};
		if (!allowed) {
			throw new InvalidRequestException(
					"Well-being report cannot transition from " + current + " to " + target + ".");
		}
	}

	public record PrivateAudio(String mediaType, byte[] bytes) {
	}
}
