package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.AlertHistory;
import com.astrahealth.backend.entity.AlertHistoryAction;

public record AlertHistoryResponse(
		Long id,
		AlertHistoryAction action,
		Long actorUserId,
		String details,
		Instant occurredAt) {

	public static AlertHistoryResponse from(AlertHistory history) {
		return new AlertHistoryResponse(
				history.getId(),
				history.getAction(),
				history.getActor() == null ? null : history.getActor().getId(),
				history.getDetails(),
				history.getOccurredAt());
	}
}
