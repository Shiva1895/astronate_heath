package com.astrahealth.backend.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MedicalObservationRequest(
		@NotNull(message = "Astronaut ID is required.")
		Long astronautId,
		@NotBlank(message = "Observation text is required.")
		@Size(max = 10000, message = "Observation text must be at most 10000 characters.")
		String observationText,
		Long relatedAlertId,
		Long relatedReadingId,
		LocalDate followUpDate) {
}
