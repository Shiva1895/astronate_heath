package com.astrahealth.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.astrahealth.backend.entity.HealthDataSource;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.ReadingType;

public record HealthReadingResponse(
		Long id,
		Long astronautId,
		Long deviceId,
		ReadingType readingType,
		BigDecimal value,
		String unit,
		Instant recordedAt,
		HealthDataSource dataSource,
		Instant createdAt) {

	public static HealthReadingResponse from(HealthReading reading) {
		return new HealthReadingResponse(
				reading.getId(),
				reading.getAstronaut().getId(),
				reading.getDevice() == null ? null : reading.getDevice().getId(),
				reading.getReadingType(),
				reading.getValue(),
				reading.getUnit(),
				reading.getRecordedAt(),
				reading.getDataSource(),
				reading.getCreatedAt());
	}
}
