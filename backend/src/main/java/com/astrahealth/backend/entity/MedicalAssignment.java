package com.astrahealth.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "medical_assignments",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_medical_assignment_officer_astronaut",
				columnNames = {"medical_officer_id", "astronaut_id"}),
		indexes = {
				@Index(name = "idx_medical_assignment_officer", columnList = "medical_officer_id"),
				@Index(name = "idx_medical_assignment_astronaut", columnList = "astronaut_id")
		})
public class MedicalAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "medical_officer_id", nullable = false)
	private UserAccount medicalOfficer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "astronaut_id", nullable = false)
	private Astronaut astronaut;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void setCreatedAt() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public UserAccount getMedicalOfficer() {
		return medicalOfficer;
	}

	public void setMedicalOfficer(UserAccount medicalOfficer) {
		this.medicalOfficer = medicalOfficer;
	}

	public Astronaut getAstronaut() {
		return astronaut;
	}

	public void setAstronaut(Astronaut astronaut) {
		this.astronaut = astronaut;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
