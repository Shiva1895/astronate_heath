package com.astrahealth.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
		name = "anomaly_rules",
		indexes = @Index(name = "idx_anomaly_rules_type_enabled", columnList = "reading_type, enabled"))
public class AnomalyRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "reading_type", nullable = false, length = 40)
	private ReadingType readingType;

	@Column(name = "minimum_value", precision = 20, scale = 6)
	private BigDecimal minimumValue;

	@Column(name = "maximum_value", precision = 20, scale = 6)
	private BigDecimal maximumValue;

	@Column(name = "change_threshold_value", precision = 20, scale = 6)
	private BigDecimal changeThresholdValue;

	@Column(name = "lookback_count", nullable = false)
	private int lookbackCount = 5;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AlertPriority priority;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(nullable = false)
	private boolean enabled;

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
	public ReadingType getReadingType() { return readingType; }
	public void setReadingType(ReadingType readingType) { this.readingType = readingType; }
	public BigDecimal getMinimumValue() { return minimumValue; }
	public void setMinimumValue(BigDecimal minimumValue) { this.minimumValue = minimumValue; }
	public BigDecimal getMaximumValue() { return maximumValue; }
	public void setMaximumValue(BigDecimal maximumValue) { this.maximumValue = maximumValue; }
	public BigDecimal getChangeThresholdValue() { return changeThresholdValue; }
	public void setChangeThresholdValue(BigDecimal changeThresholdValue) { this.changeThresholdValue = changeThresholdValue; }
	public int getLookbackCount() { return lookbackCount; }
	public void setLookbackCount(int lookbackCount) { this.lookbackCount = lookbackCount; }
	public AlertPriority getPriority() { return priority; }
	public void setPriority(AlertPriority priority) { this.priority = priority; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
