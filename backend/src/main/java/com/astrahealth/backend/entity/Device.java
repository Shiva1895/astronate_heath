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
		name = "devices",
		uniqueConstraints = @UniqueConstraint(name = "uk_devices_serial_number", columnNames = "serial_number"),
		indexes = {
				@Index(name = "idx_devices_status", columnList = "status"),
				@Index(name = "idx_devices_type", columnList = "device_type"),
				@Index(name = "idx_devices_astronaut", columnList = "astronaut_id")
		})
public class Device {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "device_name", nullable = false, length = 120)
	private String deviceName;

	@Column(name = "serial_number", nullable = false, length = 100)
	private String serialNumber;

	@Column(name = "device_type", nullable = false, length = 80)
	private String deviceType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private DeviceStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "astronaut_id")
	private Astronaut assignedAstronaut;

	@Column(name = "registered_at", nullable = false, updatable = false)
	private Instant registeredAt;

	@Column(name = "last_communication_at")
	private Instant lastCommunicationAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void initializeTimestamps() {
		Instant now = Instant.now();
		registeredAt = now;
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void updateTimestamp() {
		updatedAt = Instant.now();
	}

	public Long getId() { return id; }
	public String getDeviceName() { return deviceName; }
	public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
	public String getSerialNumber() { return serialNumber; }
	public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
	public String getDeviceType() { return deviceType; }
	public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
	public DeviceStatus getStatus() { return status; }
	public void setStatus(DeviceStatus status) { this.status = status; }
	public Astronaut getAssignedAstronaut() { return assignedAstronaut; }
	public void setAssignedAstronaut(Astronaut assignedAstronaut) { this.assignedAstronaut = assignedAstronaut; }
	public Instant getRegisteredAt() { return registeredAt; }
	public Instant getLastCommunicationAt() { return lastCommunicationAt; }
	public void setLastCommunicationAt(Instant lastCommunicationAt) { this.lastCommunicationAt = lastCommunicationAt; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
