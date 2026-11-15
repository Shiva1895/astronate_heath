package com.astrahealth.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

	@NotBlank(message = "Email is required.")
	@Email(message = "Email must be a valid email address.")
	@Size(max = 254, message = "Email must be at most 254 characters.")
	private String email;

	@Pattern(regexp = "^[A-Za-z0-9._-]{3,32}$",
			message = "Username must be 3 to 32 characters and contain only letters, numbers, dots, underscores, or hyphens.")
	private String username;

	@Size(max = 120, message = "Display name must be at most 120 characters.")
	private String displayName;

	@NotBlank(message = "Password is required.")
	@Size(min = 12, max = 72, message = "Password must be between 12 and 72 characters.")
	private String password;

	public RegisterRequest() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
