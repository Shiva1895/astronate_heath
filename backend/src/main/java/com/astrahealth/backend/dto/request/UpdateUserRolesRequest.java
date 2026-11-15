package com.astrahealth.backend.dto.request;

import java.util.Set;

import com.astrahealth.backend.entity.UserRole;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserRolesRequest(
		@NotEmpty(message = "At least one role is required.")
		Set<UserRole> roles) {
}
