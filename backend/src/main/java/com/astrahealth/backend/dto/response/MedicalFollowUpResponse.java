package com.astrahealth.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.astrahealth.backend.entity.FollowUpStatus;
import com.astrahealth.backend.entity.MedicalFollowUpAction;

public record MedicalFollowUpResponse(
		Long id,
		Long observationId,
		String actionText,
		LocalDate dueDate,
		FollowUpStatus status,
		Long createdByUserId,
		Long updatedByUserId,
		Long assignedToUserId,
		Instant createdAt,
		Instant updatedAt) {

	public static MedicalFollowUpResponse from(MedicalFollowUpAction action) {
		return new MedicalFollowUpResponse(
				action.getId(),
				action.getObservation().getId(),
				action.getActionText(),
				action.getDueDate(),
				action.getStatus(),
				action.getCreatedBy().getId(),
				action.getUpdatedBy().getId(),
				action.getAssignedTo() == null ? null : action.getAssignedTo().getId(),
				action.getCreatedAt(),
				action.getUpdatedAt());
	}
}
