package com.astrahealth.backend.dto.response;

import java.math.BigDecimal;

import com.astrahealth.backend.entity.ReadingType;

public record HealthSummaryComparison(
		ReadingType readingType,
		HealthPeriodSummary firstPeriod,
		HealthPeriodSummary secondPeriod,
		BigDecimal averageDifference,
		BigDecimal averagePercentChange) {
}
