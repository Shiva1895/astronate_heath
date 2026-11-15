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

@Entity
@Table(name = "audit_events", indexes = {
		@Index(name = "idx_audit_actor_time", columnList = "actor_id, occurred_at"),
		@Index(name = "idx_audit_resource_time", columnList = "resource_type, resource_id, occurred_at")
})
public class AuditEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_id")
	private UserAccount actor;

	@Column(nullable = false, length = 64)
	private String action;

	@Column(name = "resource_type", nullable = false, length = 64)
	private String resourceType;

	@Column(name = "resource_id")
	private Long resourceId;

	@Column(length = 500)
	private String metadata;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	@PrePersist
	void initializeTimestamp() {
		if (occurredAt == null) {
			occurredAt = Instant.now();
		}
	}

	public Long getId() { return id; }
	public UserAccount getActor() { return actor; }
	public void setActor(UserAccount actor) { this.actor = actor; }
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
	public String getResourceType() { return resourceType; }
	public void setResourceType(String resourceType) { this.resourceType = resourceType; }
	public Long getResourceId() { return resourceId; }
	public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
	public String getMetadata() { return metadata; }
	public void setMetadata(String metadata) { this.metadata = metadata; }
	public Instant getOccurredAt() { return occurredAt; }
}
