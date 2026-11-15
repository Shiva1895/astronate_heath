package com.astrahealth.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AnomalyRule;
import com.astrahealth.backend.entity.ReadingType;

public record AnomalyRuleResponse(
		Long id,
		ReadingType readingType,
		BigDecimal minimumValue,
		BigDecimal maximumValue,
		BigDecimal changeThresholdValue,
		int lookbackCount,
		AlertPriority priority,
		String description,
		boolean enabled,
		Instant createdAt,
		Instant updatedAt) {

	public static AnomalyRuleResponse from(AnomalyRule rule) {
		return new AnomalyRuleResponse(
				rule.getId(),
				rule.getReadingType(),
				rule.getMinimumValue(),
				rule.getMaximumValue(),
				rule.getChangeThresholdValue(),
				rule.getLookbackCount(),
				rule.getPriority(),
				rule.getDescription(),
				rule.isEnabled(),
				rule.getCreatedAt(),
				rule.getUpdatedAt());
	}
}
