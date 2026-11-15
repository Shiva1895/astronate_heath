package com.astrahealth.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.astrahealth.backend.entity.ReadingType;

public record HealthSummaryBucket(
		LocalDate bucketDate,
		ReadingType readingType,
		BigDecimal minimumValue,
		BigDecimal maximumValue,
		BigDecimal averageValue,
		BigDecimal latestValue,
		long readingCount,
		Instant latestRecordedAt) {
}
