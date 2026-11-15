package com.astrahealth.backend.dto.request;

import java.math.BigDecimal;
import java.time.Instant;

import com.astrahealth.backend.entity.HealthDataSource;
import com.astrahealth.backend.entity.ReadingType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateHealthReadingRequest(
		@NotNull(message = "Astronaut ID is required.")
		Long astronautId,
		Long deviceId,
		@NotNull(message = "Reading type is required.")
		ReadingType readingType,
		@NotNull(message = "Reading value is required.")
		@DecimalMin(value = "0.0", message = "Reading value cannot be negative.")
		@Digits(integer = 12, fraction = 6, message = "Reading value has too many digits.")
		BigDecimal value,
		@NotBlank(message = "Reading unit is required.")
		@Size(max = 32, message = "Reading unit must be at most 32 characters.")
		String unit,
		@NotNull(message = "Recorded timestamp is required.")
		Instant recordedAt,
		@NotNull(message = "Data source is required.")
		HealthDataSource dataSource,
		@Size(max = 128, message = "Source event ID must be at most 128 characters.")
		String sourceEventId) {
}
