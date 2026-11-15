package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.WellbeingCategory;
import com.astrahealth.backend.entity.WellbeingReport;
import com.astrahealth.backend.entity.WellbeingReportStatus;

public record WellbeingReportResponse(
		Long id,
		Long astronautId,
		WellbeingCategory category,
		WellbeingReportStatus status,
		String reportText,
		boolean hasAudio,
		Long audioRecordingId,
		String transcriptionStatus,
		Instant submittedAt,
		Instant updatedAt) {

	public static WellbeingReportResponse from(WellbeingReport report, Long audioId) {
		return new WellbeingReportResponse(
				report.getId(),
				report.getAstronaut().getId(),
				report.getCategory(),
				report.getStatus(),
				report.getReportText(),
				audioId != null,
				audioId,
				"UNAVAILABLE",
				report.getSubmittedAt(),
				report.getUpdatedAt());
	}
}
