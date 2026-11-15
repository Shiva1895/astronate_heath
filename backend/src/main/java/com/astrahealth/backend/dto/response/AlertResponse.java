package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AlertStatus;

public record AlertResponse(
		Long id,
		Long astronautId,
		Long sourceReadingId,
		Long anomalyRuleId,
		String title,
		String description,
		String reason,
		AlertPriority priority,
		AlertStatus status,
		Long createdByUserId,
		Long lastHandledByUserId,
		Instant createdAt,
		Instant updatedAt,
		Instant acknowledgedAt,
		Instant escalatedAt,
		Instant resolvedAt,
		Instant closedAt) {

	public static AlertResponse from(Alert alert) {
		return new AlertResponse(
				alert.getId(),
				alert.getAstronaut().getId(),
				alert.getSourceReading() == null ? null : alert.getSourceReading().getId(),
				alert.getAnomalyRule() == null ? null : alert.getAnomalyRule().getId(),
				alert.getTitle(),
				alert.getDescription(),
				alert.getReason(),
				alert.getPriority(),
				alert.getStatus(),
				alert.getCreatedBy() == null ? null : alert.getCreatedBy().getId(),
				alert.getLastHandledBy() == null ? null : alert.getLastHandledBy().getId(),
				alert.getCreatedAt(),
				alert.getUpdatedAt(),
				alert.getAcknowledgedAt(),
				alert.getEscalatedAt(),
				alert.getResolvedAt(),
				alert.getClosedAt());
	}
}
