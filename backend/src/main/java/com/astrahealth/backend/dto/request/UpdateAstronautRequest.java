package com.astrahealth.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateAstronautRequest {

	@NotBlank(message = "Astronaut code is required.")
	@Size(max = 50, message = "Astronaut code must be at most 50 characters.")
	private String astronautCode;

	@NotBlank(message = "Full name is required.")
	@Size(max = 150, message = "Full name must be at most 150 characters.")
	private String fullName;

	@Email(message = "Email must be a valid email address.")
	@Size(max = 254, message = "Email must be at most 254 characters.")
	private String email;

	@NotBlank(message = "Status is required.")
	@Size(max = 50, message = "Status must be at most 50 characters.")
	private String status;

	public UpdateAstronautRequest() {
	}

	public String getAstronautCode() {
		return astronautCode;
	}

	public void setAstronautCode(String astronautCode) {
		this.astronautCode = astronautCode;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
