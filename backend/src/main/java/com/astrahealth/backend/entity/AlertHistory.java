package com.astrahealth.backend.entity;

import java.time.Instant;

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
		name = "health_alert_history",
		indexes = @Index(name = "idx_alert_history_alert_time", columnList = "alert_id, occurred_at"))
public class AlertHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "alert_id", nullable = false)
	private Alert alert;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_user_id")
	private UserAccount actor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AlertHistoryAction action;

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
	public Alert getAlert() { return alert; }
	public void setAlert(Alert alert) { this.alert = alert; }
	public UserAccount getActor() { return actor; }
	public void setActor(UserAccount actor) { this.actor = actor; }
	public AlertHistoryAction getAction() { return action; }
	public void setAction(AlertHistoryAction action) { this.action = action; }
	public String getDetails() { return details; }
	public void setDetails(String details) { this.details = details; }
	public Instant getOccurredAt() { return occurredAt; }
}
