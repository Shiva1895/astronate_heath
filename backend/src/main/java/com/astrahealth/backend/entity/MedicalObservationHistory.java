package com.astrahealth.backend.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(
		name = "medical_observation_history",
		indexes = @Index(name = "idx_observation_history_time", columnList = "observation_id, occurred_at"))
public class MedicalObservationHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "observation_id", nullable = false)
	private MedicalObservation observation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "actor_user_id", nullable = false)
	private UserAccount actor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ObservationHistoryEvent event;

	@Column(name = "observation_text_snapshot", length = 10000)
	private String observationTextSnapshot;

	@Column(name = "follow_up_date_snapshot")
	private LocalDate followUpDateSnapshot;

	@Column(name = "related_alert_id_snapshot")
	private Long relatedAlertIdSnapshot;

	@Column(name = "related_reading_id_snapshot")
	private Long relatedReadingIdSnapshot;

	@Column(length = 1000)
	private String details;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	@PrePersist
	void setOccurredAt() {
		if (occurredAt == null) {
			occurredAt = Instant.now();
		}
	}

	public Long getId() { return id; }
	public MedicalObservation getObservation() { return observation; }
	public void setObservation(MedicalObservation observation) { this.observation = observation; }
	public UserAccount getActor() { return actor; }
	public void setActor(UserAccount actor) { this.actor = actor; }
	public ObservationHistoryEvent getEvent() { return event; }
	public void setEvent(ObservationHistoryEvent event) { this.event = event; }
	public String getObservationTextSnapshot() { return observationTextSnapshot; }
	public void setObservationTextSnapshot(String observationTextSnapshot) { this.observationTextSnapshot = observationTextSnapshot; }
	public LocalDate getFollowUpDateSnapshot() { return followUpDateSnapshot; }
	public void setFollowUpDateSnapshot(LocalDate followUpDateSnapshot) { this.followUpDateSnapshot = followUpDateSnapshot; }
	public Long getRelatedAlertIdSnapshot() { return relatedAlertIdSnapshot; }
	public void setRelatedAlertIdSnapshot(Long relatedAlertIdSnapshot) { this.relatedAlertIdSnapshot = relatedAlertIdSnapshot; }
	public Long getRelatedReadingIdSnapshot() { return relatedReadingIdSnapshot; }
	public void setRelatedReadingIdSnapshot(Long relatedReadingIdSnapshot) { this.relatedReadingIdSnapshot = relatedReadingIdSnapshot; }
	public String getDetails() { return details; }
	public void setDetails(String details) { this.details = details; }
	public Instant getOccurredAt() { return occurredAt; }
}
