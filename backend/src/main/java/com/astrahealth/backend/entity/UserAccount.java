package com.astrahealth.backend.entity;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_accounts", uniqueConstraints = {
		@UniqueConstraint(name = "uk_user_accounts_username", columnNames = "username"),
		@UniqueConstraint(name = "uk_user_accounts_astronaut_profile", columnNames = "astronaut_id")
})
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(length = 32)
	private String username;

	@Column(name = "display_name", length = 120)
	private String displayName;

	@JsonIgnore
	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@OneToOne
	@JoinColumn(name = "astronaut_id")
	private Astronaut astronautProfile;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_account_roles", joinColumns = @JoinColumn(name = "user_account_id"))
	@Column(name = "role", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private Set<UserRole> roles = EnumSet.noneOf(UserRole.class);

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AccountStatus status = AccountStatus.PENDING_ACTIVATION;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public UserAccount() {
	}

	@PrePersist
	void setCreatedAtIfMissing() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Set<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public Astronaut getAstronautProfile() {
		return astronautProfile;
	}

	public void setAstronautProfile(Astronaut astronautProfile) {
		this.astronautProfile = astronautProfile;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
