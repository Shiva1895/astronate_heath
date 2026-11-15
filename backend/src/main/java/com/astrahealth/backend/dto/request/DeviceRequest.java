package com.astrahealth.backend.dto.request;

import com.astrahealth.backend.entity.DeviceStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceRequest(
		@NotBlank(message = "Device name is required.")
		@Size(max = 120, message = "Device name must be at most 120 characters.")
		String deviceName,
		@NotBlank(message = "Serial number is required.")
		@Size(max = 100, message = "Serial number must be at most 100 characters.")
		String serialNumber,
		@NotBlank(message = "Device type is required.")
		@Size(max = 80, message = "Device type must be at most 80 characters.")
		String deviceType,
		@NotNull(message = "Device status is required.")
		DeviceStatus status) {
}
