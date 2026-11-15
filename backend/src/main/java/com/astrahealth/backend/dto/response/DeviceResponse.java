package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.Device;
import com.astrahealth.backend.entity.DeviceStatus;

public record DeviceResponse(
		Long id,
		String deviceName,
		String serialNumber,
		String deviceType,
		DeviceStatus status,
		Long astronautId,
		String astronautCode,
		Instant registeredAt,
		Instant lastCommunicationAt,
		Instant createdAt,
		Instant updatedAt) {

	public static DeviceResponse from(Device device) {
		return new DeviceResponse(
				device.getId(),
				device.getDeviceName(),
				device.getSerialNumber(),
				device.getDeviceType(),
				device.getStatus(),
				device.getAssignedAstronaut() == null ? null : device.getAssignedAstronaut().getId(),
				device.getAssignedAstronaut() == null
						? null
						: device.getAssignedAstronaut().getAstronautCode(),
				device.getRegisteredAt(),
				device.getLastCommunicationAt(),
				device.getCreatedAt(),
				device.getUpdatedAt());
	}
}
