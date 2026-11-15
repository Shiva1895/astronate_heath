package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.DeviceEventType;
import com.astrahealth.backend.entity.DeviceHistory;

public record DeviceHistoryResponse(
		Long id,
		DeviceEventType eventType,
		Long astronautId,
		String astronautCode,
		Instant occurredAt) {

	public static DeviceHistoryResponse from(DeviceHistory history) {
		return new DeviceHistoryResponse(
				history.getId(),
				history.getEventType(),
				history.getAstronaut() == null ? null : history.getAstronaut().getId(),
				history.getAstronaut() == null ? null : history.getAstronaut().getAstronautCode(),
				history.getOccurredAt());
	}
}
