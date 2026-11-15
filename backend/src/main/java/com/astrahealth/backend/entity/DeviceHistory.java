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
		name = "device_history",
		indexes = {
				@Index(name = "idx_device_history_device_time", columnList = "device_id, occurred_at"),
				@Index(name = "idx_device_history_astronaut", columnList = "astronaut_id")
		})
public class DeviceHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "device_id", nullable = false)
	private Device device;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "astronaut_id")
	private Astronaut astronaut;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 32)
	private DeviceEventType eventType;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	@PrePersist
	void setOccurredAt() {
		if (occurredAt == null) {
			occurredAt = Instant.now();
		}
	}

	public Long getId() { return id; }
	public Device getDevice() { return device; }
	public void setDevice(Device device) { this.device = device; }
	public Astronaut getAstronaut() { return astronaut; }
	public void setAstronaut(Astronaut astronaut) { this.astronaut = astronaut; }
	public DeviceEventType getEventType() { return eventType; }
	public void setEventType(DeviceEventType eventType) { this.eventType = eventType; }
	public Instant getOccurredAt() { return occurredAt; }
}
