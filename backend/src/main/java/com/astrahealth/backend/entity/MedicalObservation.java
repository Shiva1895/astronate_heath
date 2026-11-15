package com.astrahealth.backend.entity;

import java.time.Instant;
import java.time.LocalDate;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
		name = "medical_observations",
		indexes = {
				@Index(name = "idx_medical_observations_astronaut_created", columnList = "astronaut_id, created_at"),
				@Index(name = "idx_medical_observations_officer_created", columnList = "medical_officer_id, created_at")
		})
public class MedicalObservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "astronaut_id", nullable = false)
	private Astronaut astronaut;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "medical_officer_id", nullable = false)
	private UserAccount medicalOfficer;

	@Column(name = "observation_text", nullable = false, length = 10000)
	private String observationText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "related_alert_id")
	private Alert relatedAlert;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "related_reading_id")
	private HealthReading relatedReading;

	@Column(name = "follow_up_date")
	private LocalDate followUpDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void initializeTimestamps() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void updateTimestamp() {
		updatedAt = Instant.now();
	}

	public Long getId() { return id; }
	public Astronaut getAstronaut() { return astronaut; }
	public void setAstronaut(Astronaut astronaut) { this.astronaut = astronaut; }
	public UserAccount getMedicalOfficer() { return medicalOfficer; }
	public void setMedicalOfficer(UserAccount medicalOfficer) { this.medicalOfficer = medicalOfficer; }
	public String getObservationText() { return observationText; }
	public void setObservationText(String observationText) { this.observationText = observationText; }
	public Alert getRelatedAlert() { return relatedAlert; }
	public void setRelatedAlert(Alert relatedAlert) { this.relatedAlert = relatedAlert; }
	public HealthReading getRelatedReading() { return relatedReading; }
	public void setRelatedReading(HealthReading relatedReading) { this.relatedReading = relatedReading; }
	public LocalDate getFollowUpDate() { return followUpDate; }
	public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
