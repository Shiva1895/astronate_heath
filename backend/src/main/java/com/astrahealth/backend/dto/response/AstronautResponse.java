package com.astrahealth.backend.dto.response;

public class AstronautResponse {

	private final Long id;
	private final String astronautCode;
	private final String fullName;
	private final String email;
	private final String status;

	public AstronautResponse(Long id, String astronautCode, String fullName, String email, String status) {
		this.id = id;
		this.astronautCode = astronautCode;
		this.fullName = fullName;
		this.email = email;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public String getAstronautCode() {
		return astronautCode;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getStatus() {
		return status;
	}
}
