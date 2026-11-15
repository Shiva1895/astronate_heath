package com.astrahealth.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateMedicalAssignmentRequest(
		@NotNull(message = "Medical officer user ID is required.")
		Long medicalOfficerUserId,
		@NotNull(message = "Astronaut ID is required.")
		Long astronautId) {
}
