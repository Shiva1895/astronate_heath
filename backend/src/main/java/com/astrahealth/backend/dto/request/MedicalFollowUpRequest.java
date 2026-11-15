package com.astrahealth.backend.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalFollowUpRequest(
		@NotBlank(message = "Follow-up action text is required.")
		@Size(max = 2000, message = "Follow-up action text must be at most 2000 characters.")
		String actionText,
		LocalDate dueDate,
		Long assignedToUserId) {

	public MedicalFollowUpRequest(String actionText, LocalDate dueDate) {
		this(actionText, dueDate, null);
	}
}
