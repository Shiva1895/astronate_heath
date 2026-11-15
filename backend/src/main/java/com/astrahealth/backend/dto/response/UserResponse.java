package com.astrahealth.backend.dto.response;

import java.time.Instant;
import java.util.Set;

import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;

public record UserResponse(
		Long id,
		String email,
		String username,
		String displayName,
		AccountStatus status,
		Set<UserRole> roles,
		Long astronautProfileId,
		Instant createdAt) {

	public static UserResponse from(UserAccount account) {
		return new UserResponse(
				account.getId(),
				account.getEmail(),
				account.getUsername(),
				account.getDisplayName(),
				account.getStatus(),
				Set.copyOf(account.getRoles()),
				account.getAstronautProfile() == null ? null : account.getAstronautProfile().getId(),
				account.getCreatedAt());
	}
}
