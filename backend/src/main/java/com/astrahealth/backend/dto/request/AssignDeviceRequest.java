package com.astrahealth.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignDeviceRequest(
		@NotNull(message = "Astronaut ID is required.")
		Long astronautId) {
}
