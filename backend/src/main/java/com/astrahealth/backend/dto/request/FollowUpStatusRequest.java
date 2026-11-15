package com.astrahealth.backend.dto.request;

import com.astrahealth.backend.entity.FollowUpStatus;

import jakarta.validation.constraints.NotNull;

public record FollowUpStatusRequest(
		@NotNull(message = "Follow-up status is required.")
		FollowUpStatus status) {
}
