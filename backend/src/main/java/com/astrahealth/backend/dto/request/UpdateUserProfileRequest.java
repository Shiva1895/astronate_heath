package com.astrahealth.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
		@Pattern(regexp = "^[A-Za-z0-9._-]{3,32}$",
				message = "Username must be 3 to 32 characters and contain only letters, numbers, dots, underscores, or hyphens.")
		String username,
		@NotBlank(message = "Display name is required.")
		@Size(max = 120, message = "Display name must be at most 120 characters.")
		String displayName) {
}
