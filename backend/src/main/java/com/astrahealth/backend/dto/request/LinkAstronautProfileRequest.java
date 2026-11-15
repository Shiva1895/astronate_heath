package com.astrahealth.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record LinkAstronautProfileRequest(
		@NotNull(message = "Astronaut ID is required.")
		Long astronautId) {
}
