package com.astrahealth.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.MedicalFollowUpAction;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.entity.WellbeingAudioRecording;
import com.astrahealth.backend.entity.WellbeingReport;
import com.astrahealth.backend.entity.WellbeingReportStatus;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
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
import com.astrahealth.backend.dto.response.MissionOperationalSummary;

@Service
@Transactional(readOnly = true)
public class ReportExportService {

	private static final int EXPORT_LIMIT = 5000;
	private static final Duration MAX_HEALTH_RANGE = Duration.ofDays(366L * 5);

	private final HealthReadingRepository readingRepository;
	private final AlertRepository alertRepository;
	private final MedicalFollowUpActionRepository followUpRepository;
	private final WellbeingReportRepository wellbeingRepository;
	private final WellbeingAudioRecordingRepository audioRepository;
	private final AstronautRepository astronautRepository;
	private final MissionRepository missionRepository;
	private final UserAccountRepository userAccountRepository;
	private final MedicalAssignmentRepository assignmentRepository;
	private final HealthAccessService healthAccessService;
	private final HealthDashboardService dashboardService;

	public ReportExportService(
			HealthReadingRepository readingRepository,
			AlertRepository alertRepository,
			MedicalFollowUpActionRepository followUpRepository,
			WellbeingReportRepository wellbeingRepository,
			WellbeingAudioRecordingRepository audioRepository,
			AstronautRepository astronautRepository,
			MissionRepository missionRepository,
			UserAccountRepository userAccountRepository,
			MedicalAssignmentRepository assignmentRepository,
			HealthAccessService healthAccessService,
			HealthDashboardService dashboardService) {
		this.readingRepository = readingRepository;
		this.alertRepository = alertRepository;
		this.followUpRepository = followUpRepository;
		this.wellbeingRepository = wellbeingRepository;
		this.audioRepository = audioRepository;
		this.astronautRepository = astronautRepository;
		this.missionRepository = missionRepository;
		this.userAccountRepository = userAccountRepository;
		this.assignmentRepository = assignmentRepository;
		this.healthAccessService = healthAccessService;
		this.dashboardService = dashboardService;
	}

	public ExportFile health(
			Long astronautId,
			Instant from,
			Instant to,
			ReportFormat format,
			Long userId) {
		requireAstronaut(astronautId);
		healthAccessService.requireAccess(astronautId, userId);
		validateRange(from, to);
		Page<HealthReading> page = readingRepository.search(
				astronautId,
				null,
				from,
				to,
				PageRequest.of(0, EXPORT_LIMIT + 1, Sort.by(Sort.Direction.ASC, "recordedAt")));
		requireWithinLimit(page);
		List<List<String>> rows = new ArrayList<>();
		rows.add(List.of("id", "reading_type", "value", "unit", "recorded_at_utc", "data_source", "device_id"));
		page.getContent().forEach(reading -> rows.add(List.of(
				value(reading.getId()),
				value(reading.getReadingType()),
				value(reading.getValue()),
				value(reading.getUnit()),
				value(reading.getRecordedAt()),
				value(reading.getDataSource()),
				value(reading.getDevice() == null ? null : reading.getDevice().getId()))));
		return export(rows, "astronaut-health", format);
	}

	public ExportFile mission(Long missionId, ReportFormat format, Long userId) {
		UserAccount actor = requireUser(userId);
		if (!actor.getRoles().contains(UserRole.ADMIN) && !actor.getRoles().contains(UserRole.MISSION_CONTROL)) {
			throw new AccessDeniedException("Only administrators and mission control can export mission reports.");
		}
		if (missionRepository.findByIdAndArchivedAtIsNull(missionId).isEmpty()) {
			throw new ResourceNotFoundException("Mission not found with ID: " + missionId);
		}
		MissionOperationalSummary summary = dashboardService.missionSummary(missionId);
		List<List<String>> rows = List.of(
				List.of("id", "mission_code", "mission_name", "status", "astronaut_count",
						"active_alert_count", "offline_device_count", "latest_reading_at_utc"),
				List.of(value(summary.missionId()), value(summary.missionCode()), value(summary.missionName()),
						value(summary.status()), value(summary.astronautCount()), value(summary.activeAlertCount()),
						value(summary.offlineDeviceCount()), value(summary.latestReadingAt())));
		return export(rows, "mission-operational-summary", format);
	}

	public ExportFile alerts(Long astronautId, ReportFormat format, Long userId) {
		if (astronautId == null) {
			if (!requireUser(userId).getRoles().contains(UserRole.ADMIN)) {
				throw new AccessDeniedException("An astronaut ID is required for this alert report.");
			}
		} else {
			requireAstronaut(astronautId);
			healthAccessService.requireAccess(astronautId, userId);
		}
		Page<Alert> page = alertRepository.search(
				astronautId,
				null,
				null,
				PageRequest.of(0, EXPORT_LIMIT + 1, Sort.by(Sort.Direction.DESC, "createdAt")));
		requireWithinLimit(page);
		List<List<String>> rows = new ArrayList<>();
		rows.add(List.of("id", "astronaut_id", "title", "reason", "priority", "status", "created_at_utc"));
		page.getContent().forEach(alert -> rows.add(List.of(
				value(alert.getId()),
				value(alert.getAstronaut().getId()),
				value(alert.getTitle()),
				value(alert.getReason()),
				value(alert.getPriority()),
				value(alert.getStatus()),
				value(alert.getCreatedAt()))));
		return export(rows, "alerts", format);
	}

	public ExportFile followUps(Long astronautId, ReportFormat format, Long userId) {
		if (astronautId == null) {
			throw new InvalidRequestException("An astronaut ID is required for follow-up reports.");
		}
		requireAstronaut(astronautId);
		requireAssignedMedicalAccess(userId, astronautId);
		Page<MedicalFollowUpAction> actionPage = followUpRepository
				.findAllByObservationAstronautIdOrderByDueDateAsc(
						astronautId, PageRequest.of(0, EXPORT_LIMIT + 1));
		requireWithinLimit(actionPage);
		List<List<String>> rows = new ArrayList<>();
		rows.add(List.of("id", "observation_id", "action", "due_date", "status",
				"created_by_user_id", "assigned_to_user_id"));
		actionPage.getContent().forEach(action -> rows.add(List.of(
				value(action.getId()),
				value(action.getObservation().getId()),
				value(action.getActionText()),
				value(action.getDueDate()),
				value(action.getStatus()),
				value(action.getCreatedBy().getId()),
				value(action.getAssignedTo() == null ? null : action.getAssignedTo().getId()))));
		return export(rows, "medical-follow-ups", format);
	}

	public ExportFile wellbeing(
			Long astronautId,
			WellbeingReportStatus status,
			ReportFormat format,
			Long userId) {
		requireAstronaut(astronautId);
		requireWellbeingAccess(userId, astronautId);
		Page<WellbeingReport> page = status == null
				? wellbeingRepository.findAllByAstronautIdOrderBySubmittedAtDesc(
						astronautId, PageRequest.of(0, EXPORT_LIMIT + 1))
				: wellbeingRepository.findAllByAstronautIdAndStatusOrderBySubmittedAtDesc(
						astronautId, status, PageRequest.of(0, EXPORT_LIMIT + 1));
		requireWithinLimit(page);
		List<Long> reportIds = page.getContent().stream().map(WellbeingReport::getId).toList();
		Set<Long> audioReportIds = reportIds.isEmpty()
				? Set.of()
				: audioRepository.findAllByReportIdIn(reportIds).stream()
				.map(recording -> recording.getReport().getId())
				.collect(java.util.stream.Collectors.toSet());
		List<List<String>> rows = new ArrayList<>();
		rows.add(List.of("id", "category", "status", "report_text", "has_audio", "submitted_at_utc"));
		page.getContent().forEach(report -> rows.add(List.of(
				value(report.getId()),
				value(report.getCategory()),
				value(report.getStatus()),
				value(report.getReportText()),
				Boolean.toString(audioReportIds.contains(report.getId())),
				value(report.getSubmittedAt()))));
		return export(rows, "wellbeing-reports", format);
	}

	private ExportFile export(List<List<String>> rows, String baseName, ReportFormat format) {
		if (format == null) {
			throw new InvalidRequestException("Export format must be CSV or PDF.");
		}
		String extension = format.name().toLowerCase(Locale.ROOT);
		byte[] contents = format == ReportFormat.CSV ? toCsv(rows) : toPdf(baseName, rows);
		String contentType = format == ReportFormat.CSV ? "text/csv; charset=UTF-8" : "application/pdf";
		return new ExportFile(baseName + "." + extension, contentType, contents);
	}

	private byte[] toCsv(List<List<String>> rows) {
		StringBuilder output = new StringBuilder();
		for (List<String> row : rows) {
			for (int index = 0; index < row.size(); index++) {
				if (index > 0) output.append(',');
				output.append(csvCell(row.get(index)));
			}
			output.append("\r\n");
		}
		return output.toString().getBytes(StandardCharsets.UTF_8);
	}

	private String csvCell(String value) {
		String cell = value == null ? "" : value;
		if (cell.stripLeading().matches("^[=+@\\-].*")) {
			cell = "'" + cell;
		}
		return "\"" + cell.replace("\"", "\"\"") + "\"";
	}

	private byte[] toPdf(String title, List<List<String>> rows) {
		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
			PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
			List<String> lines = new ArrayList<>();
			lines.add("ASTRA HEALTH - " + title);
			lines.add("Generated " + Instant.now());
			rows.forEach(row -> lines.add(String.join(" | ", row)));
			for (int start = 0; start < lines.size(); start += 48) {
				PDPage page = new PDPage(PDRectangle.LETTER);
				document.addPage(page);
				try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
					stream.beginText();
					stream.setFont(bold, 12);
					stream.newLineAtOffset(36, 750);
					stream.showText(toPdfText(lines.get(start)));
					stream.setFont(font, 8);
					for (int index = start + 1; index < Math.min(start + 48, lines.size()); index++) {
						stream.newLineAtOffset(0, -14);
						stream.showText(toPdfText(lines.get(index)));
					}
					stream.endText();
				}
			}
			document.save(output);
			return output.toByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException("The PDF report could not be generated.", exception);
		}
	}

	private String toPdfText(String value) {
		String safe = (value == null ? "" : value)
				.replaceAll("[^\\x20-\\x7E]", "?");
		return safe.length() > 150 ? safe.substring(0, 147) + "..." : safe;
	}

	private void validateRange(Instant from, Instant to) {
		if (from == null || to == null || !from.isBefore(to) || Duration.between(from, to).compareTo(MAX_HEALTH_RANGE) > 0) {
			throw new InvalidRequestException("Provide a valid UTC range of no more than five years.");
		}
	}

	private void requireWithinLimit(Page<?> page) {
		if (page.getContent().size() > EXPORT_LIMIT) {
			throw new InvalidRequestException("The export contains too many rows; narrow the report scope.");
		}
	}

	private Astronaut requireAstronaut(Long astronautId) {
		return astronautRepository.findById(astronautId)
				.orElseThrow(() -> new ResourceNotFoundException("Astronaut not found with ID: " + astronautId));
	}

	private UserAccount requireUser(Long userId) {
		return userAccountRepository.findById(userId)
				.orElseThrow(() -> new AccessDeniedException("The authenticated account cannot export this report."));
	}

	private void requireAssignedMedicalAccess(Long userId, Long astronautId) {
		UserAccount actor = requireUser(userId);
		if (!actor.getRoles().contains(UserRole.ADMIN)
				&& !(actor.getRoles().contains(UserRole.MEDICAL_OFFICER)
						&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(userId, astronautId))) {
			throw new AccessDeniedException("Only an administrator or assigned medical officer can export follow-ups.");
		}
	}

	private void requireWellbeingAccess(Long userId, Long astronautId) {
		UserAccount actor = requireUser(userId);
		if (!actor.getRoles().contains(UserRole.ADMIN)
				&& !actor.getRoles().contains(UserRole.MEDICAL_OFFICER)
				&& !actor.getRoles().contains(UserRole.ASTRONAUT)) {
			throw new AccessDeniedException("The authenticated account cannot export these reports.");
		}
		if (actor.getRoles().contains(UserRole.ADMIN)) {
			return;
		}
		if ((actor.getRoles().contains(UserRole.ASTRONAUT)
						&& actor.getAstronautProfile() != null
						&& actor.getAstronautProfile().getId().equals(astronautId))
				|| (actor.getRoles().contains(UserRole.MEDICAL_OFFICER)
						&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(userId, astronautId))) {
			return;
		}
		throw new AccessDeniedException("The authenticated account cannot export these reports.");
	}

	private String value(Object value) {
		return value == null ? "" : value.toString();
	}

	public record ExportFile(String fileName, String contentType, byte[] contents) {
	}
}
