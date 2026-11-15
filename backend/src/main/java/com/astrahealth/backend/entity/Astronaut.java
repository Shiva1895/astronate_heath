package com.astrahealth.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Marks this class as a JPA-managed entity mapped to the astronauts table.
@Entity
@Table(name = "astronauts")
public class Astronaut {

	// Maps the primary key and delegates ID generation to PostgreSQL.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String astronautCode;

	@Column(nullable = false)
	private String fullName;

	@Column(unique = true)
	private String email;

	@Column(nullable = false)
	private String status;

	public Astronaut() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
