package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.AuditEvent;

public record AuditEventResponse(
		Long id,
		Long actorId,
		String action,
		String resourceType,
		Long resourceId,
		String metadata,
		Instant occurredAt) {

	public static AuditEventResponse from(AuditEvent event) {
		return new AuditEventResponse(
				event.getId(),
				event.getActor() == null ? null : event.getActor().getId(),
				event.getAction(),
				event.getResourceType(),
				event.getResourceId(),
				event.getMetadata(),
				event.getOccurredAt());
	}
}
