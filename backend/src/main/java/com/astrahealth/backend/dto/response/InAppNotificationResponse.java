package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.InAppNotification;
import com.astrahealth.backend.entity.NotificationType;

public record InAppNotificationResponse(
		Long id,
		NotificationType type,
		String title,
		String message,
		String resourceType,
		Long resourceId,
		Instant createdAt,
		Instant readAt) {

	public static InAppNotificationResponse from(InAppNotification notification) {
		return new InAppNotificationResponse(
				notification.getId(),
				notification.getType(),
				notification.getTitle(),
				notification.getMessage(),
				notification.getResourceType(),
				notification.getResourceId(),
				notification.getCreatedAt(),
				notification.getReadAt());
	}
}
