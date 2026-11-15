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
@Table(name = "in_app_notifications", indexes = {
		@Index(name = "idx_notifications_recipient_created", columnList = "recipient_id, created_at"),
		@Index(name = "idx_notifications_recipient_read", columnList = "recipient_id, read_at")
})
public class InAppNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipient_id", nullable = false)
	private UserAccount recipient;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private NotificationType type;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(nullable = false, length = 500)
	private String message;

	@Column(name = "resource_type", length = 64)
	private String resourceType;

	@Column(name = "resource_id")
	private Long resourceId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "read_at")
	private Instant readAt;

	@PrePersist
	void initializeTimestamp() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() { return id; }
	public UserAccount getRecipient() { return recipient; }
	public void setRecipient(UserAccount recipient) { this.recipient = recipient; }
	public NotificationType getType() { return type; }
	public void setType(NotificationType type) { this.type = type; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public String getResourceType() { return resourceType; }
	public void setResourceType(String resourceType) { this.resourceType = resourceType; }
	public Long getResourceId() { return resourceId; }
	public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getReadAt() { return readAt; }
	public void setReadAt(Instant readAt) { this.readAt = readAt; }
}
