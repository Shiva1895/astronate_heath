package com.astrahealth.backend.dto.request;

import com.astrahealth.backend.entity.WellbeingCategory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WellbeingReportRequest(
		@NotNull(message = "Feeling category is required.")
		WellbeingCategory category,
		@Size(max = 10000, message = "Report text must be at most 10000 characters.")
		String reportText,
		Long audioRecordingId) {
}
