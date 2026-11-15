package com.astrahealth.backend.dto.request;

import com.astrahealth.backend.entity.DeviceStatus;

import jakarta.validation.constraints.NotNull;

public record DeviceStatusRequest(
		@NotNull(message = "Device status is required.")
		DeviceStatus status) {
}
