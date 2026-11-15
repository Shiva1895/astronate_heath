package com.astrahealth.backend.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "health_readings",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_health_readings_source_event_id",
				columnNames = "source_event_id"),
		indexes = {
				@Index(name = "idx_health_readings_astronaut_recorded", columnList = "astronaut_id, recorded_at"),
				@Index(
						name = "idx_health_readings_astronaut_type_recorded",
						columnList = "astronaut_id, reading_type, recorded_at"),
				@Index(name = "idx_health_readings_device_recorded", columnList = "device_id, recorded_at")
		})
public class HealthReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "astronaut_id", nullable = false)
	private Astronaut astronaut;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "device_id")
	private Device device;

	@Enumerated(EnumType.STRING)
	@Column(name = "reading_type", nullable = false, length = 40)
	private ReadingType readingType;

	@Column(name = "reading_value", nullable = false, precision = 20, scale = 6)
	private BigDecimal value;

	@Column(nullable = false, length = 32)
	private String unit;

	@Column(name = "recorded_at", nullable = false)
	private Instant recordedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "data_source", nullable = false, length = 24)
	private HealthDataSource dataSource;

	@Column(name = "source_event_id", length = 128)
	private String sourceEventId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public HealthReading() {
	}

	@PrePersist
	void initializeCreatedAt() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() { return id; }
	public Astronaut getAstronaut() { return astronaut; }
	public void setAstronaut(Astronaut astronaut) { this.astronaut = astronaut; }
	public Device getDevice() { return device; }
	public void setDevice(Device device) { this.device = device; }
	public ReadingType getReadingType() { return readingType; }
	public void setReadingType(ReadingType readingType) { this.readingType = readingType; }
	public BigDecimal getValue() { return value; }
	public void setValue(BigDecimal value) { this.value = value; }
	public String getUnit() { return unit; }
	public void setUnit(String unit) { this.unit = unit; }
	public Instant getRecordedAt() { return recordedAt; }
	public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
	public HealthDataSource getDataSource() { return dataSource; }
	public void setDataSource(HealthDataSource dataSource) { this.dataSource = dataSource; }
	public String getSourceEventId() { return sourceEventId; }
	public void setSourceEventId(String sourceEventId) { this.sourceEventId = sourceEventId; }
	public Instant getCreatedAt() { return createdAt; }
}
