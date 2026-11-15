package com.astrahealth.backend.dto.request;

import java.time.LocalDate;

import com.astrahealth.backend.entity.WellbeingReportStatus;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record WellbeingReviewRequest(
		@NotNull(message = "A report status is required.")
		WellbeingReportStatus status,
		@Size(max = 2000, message = "Review notes must be at most 2000 characters.")
		String reviewNotes,
		@Size(max = 10000, message = "Observation text must be at most 10000 characters.")
		String observationText,
		Long followUpAssigneeId,
		LocalDate followUpDueDate) {
}
