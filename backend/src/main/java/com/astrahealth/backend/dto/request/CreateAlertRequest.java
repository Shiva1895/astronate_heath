package com.astrahealth.backend.dto.request;

import com.astrahealth.backend.entity.AlertPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAlertRequest(
		@NotNull(message = "Astronaut ID is required.")
		Long astronautId,
		@NotBlank(message = "Alert title is required.")
		@Size(max = 160, message = "Alert title must be at most 160 characters.")
		String title,
		@NotBlank(message = "Alert description is required.")
		@Size(max = 2000, message = "Alert description must be at most 2000 characters.")
		String description,
		@NotBlank(message = "Alert reason is required.")
		@Size(max = 1000, message = "Alert reason must be at most 1000 characters.")
		String reason,
		@NotNull(message = "Alert priority is required.")
		AlertPriority priority) {
}
