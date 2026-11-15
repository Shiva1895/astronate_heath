package com.astrahealth.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record HealthPeriodSummary(
		Instant from,
		Instant to,
		BigDecimal minimumValue,
		BigDecimal maximumValue,
		BigDecimal averageValue,
		BigDecimal latestValue,
		long readingCount) {
}
