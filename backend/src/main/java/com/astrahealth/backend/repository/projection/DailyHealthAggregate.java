package com.astrahealth.backend.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface DailyHealthAggregate {
	LocalDate getBucketDate();
	String getReadingType();
	BigDecimal getMinimumValue();
	BigDecimal getMaximumValue();
	BigDecimal getAverageValue();
	BigDecimal getLatestValue();
	Long getReadingCount();
	OffsetDateTime getLatestRecordedAt();
}
