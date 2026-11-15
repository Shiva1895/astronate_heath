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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "health_alerts",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_alert_reading_rule",
				columnNames = {"source_reading_id", "anomaly_rule_id"}),
		indexes = {
				@Index(name = "idx_alerts_astronaut_status", columnList = "astronaut_id, status"),
				@Index(name = "idx_alerts_priority_status", columnList = "priority, status"),
				@Index(name = "idx_alerts_created_at", columnList = "created_at")
		})
public class Alert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "astronaut_id", nullable = false)
	private Astronaut astronaut;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_reading_id")
	private HealthReading sourceReading;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "anomaly_rule_id")
	private AnomalyRule anomalyRule;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(nullable = false, length = 2000)
	private String description;

	@Column(nullable = false, length = 1000)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AlertPriority priority;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AlertStatus status = AlertStatus.OPEN;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_user_id")
	private UserAccount createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_handled_by_user_id")
	private UserAccount lastHandledBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "acknowledged_at")
	private Instant acknowledgedAt;

	@Column(name = "escalated_at")
	private Instant escalatedAt;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	@Column(name = "closed_at")
	private Instant closedAt;

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
	public HealthReading getSourceReading() { return sourceReading; }
	public void setSourceReading(HealthReading sourceReading) { this.sourceReading = sourceReading; }
	public AnomalyRule getAnomalyRule() { return anomalyRule; }
	public void setAnomalyRule(AnomalyRule anomalyRule) { this.anomalyRule = anomalyRule; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
	public AlertPriority getPriority() { return priority; }
	public void setPriority(AlertPriority priority) { this.priority = priority; }
	public AlertStatus getStatus() { return status; }
	public void setStatus(AlertStatus status) { this.status = status; }
	public UserAccount getCreatedBy() { return createdBy; }
	public void setCreatedBy(UserAccount createdBy) { this.createdBy = createdBy; }
	public UserAccount getLastHandledBy() { return lastHandledBy; }
	public void setLastHandledBy(UserAccount lastHandledBy) { this.lastHandledBy = lastHandledBy; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	public Instant getAcknowledgedAt() { return acknowledgedAt; }
	public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
	public Instant getEscalatedAt() { return escalatedAt; }
	public void setEscalatedAt(Instant escalatedAt) { this.escalatedAt = escalatedAt; }
	public Instant getResolvedAt() { return resolvedAt; }
	public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
	public Instant getClosedAt() { return closedAt; }
	public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}
