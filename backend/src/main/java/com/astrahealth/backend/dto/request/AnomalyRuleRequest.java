package com.astrahealth.backend.dto.request;

import java.math.BigDecimal;

import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.ReadingType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnomalyRuleRequest(
		@NotNull(message = "Reading type is required.")
		ReadingType readingType,
		@DecimalMin(value = "0.0", message = "Minimum value cannot be negative.")
		@Digits(integer = 12, fraction = 6, message = "Minimum value has too many digits.")
		BigDecimal minimumValue,
		@DecimalMin(value = "0.0", message = "Maximum value cannot be negative.")
		@Digits(integer = 12, fraction = 6, message = "Maximum value has too many digits.")
		BigDecimal maximumValue,
		@DecimalMin(value = "0.0", inclusive = false, message = "Change threshold must be positive.")
		@Digits(integer = 12, fraction = 6, message = "Change threshold has too many digits.")
		BigDecimal changeThresholdValue,
		Integer lookbackCount,
		@NotNull(message = "Alert priority is required.")
		AlertPriority priority,
		@NotBlank(message = "Rule description is required.")
		@Size(max = 500, message = "Rule description must be at most 500 characters.")
		String description,
		boolean enabled) {

		@AssertTrue(message = "At least one threshold is required, and minimum must be below maximum.")
		public boolean isThresholdConfigurationValid() {
			boolean hasThreshold = minimumValue != null || maximumValue != null || changeThresholdValue != null;
			boolean validRange = minimumValue == null || maximumValue == null
					|| minimumValue.compareTo(maximumValue) < 0;
			boolean validLookback = lookbackCount == null || (lookbackCount >= 1 && lookbackCount <= 100);
			return hasThreshold && validRange && validLookback;
		}
	}
