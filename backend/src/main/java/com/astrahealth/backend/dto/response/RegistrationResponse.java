package com.astrahealth.backend.dto.response;

import java.util.Set;

import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.UserRole;

public class RegistrationResponse {

	private final Long id;
	private final String email;
	private final AccountStatus status;
	private final Set<UserRole> roles;

	public RegistrationResponse(Long id, String email, AccountStatus status, Set<UserRole> roles) {
		this.id = id;
		this.email = email;
		this.status = status;
		this.roles = Set.copyOf(roles);
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public Set<UserRole> getRoles() {
		return roles;
	}
}
