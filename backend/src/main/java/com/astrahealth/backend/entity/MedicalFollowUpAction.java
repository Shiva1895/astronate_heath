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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
		name = "medical_follow_up_actions",
		indexes = @Index(name = "idx_follow_up_observation_created", columnList = "observation_id, created_at"))
public class MedicalFollowUpAction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "observation_id", nullable = false)
	private MedicalObservation observation;

	@Column(name = "action_text", nullable = false, length = 2000)
	private String actionText;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private FollowUpStatus status = FollowUpStatus.PENDING;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_user_id", nullable = false)
	private UserAccount createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "updated_by_user_id", nullable = false)
	private UserAccount updatedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_to_user_id")
	private UserAccount assignedTo;

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
	public MedicalObservation getObservation() { return observation; }
	public void setObservation(MedicalObservation observation) { this.observation = observation; }
	public String getActionText() { return actionText; }
	public void setActionText(String actionText) { this.actionText = actionText; }
	public LocalDate getDueDate() { return dueDate; }
	public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
	public FollowUpStatus getStatus() { return status; }
	public void setStatus(FollowUpStatus status) { this.status = status; }
	public UserAccount getCreatedBy() { return createdBy; }
	public void setCreatedBy(UserAccount createdBy) { this.createdBy = createdBy; }
	public UserAccount getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(UserAccount updatedBy) { this.updatedBy = updatedBy; }
	public UserAccount getAssignedTo() { return assignedTo; }
	public void setAssignedTo(UserAccount assignedTo) { this.assignedTo = assignedTo; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
