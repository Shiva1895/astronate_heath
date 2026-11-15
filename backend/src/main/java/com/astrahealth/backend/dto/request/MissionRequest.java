package com.astrahealth.backend.dto.request;

import java.time.LocalDate;

import com.astrahealth.backend.entity.MissionStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MissionRequest(
		@NotBlank(message = "Mission code is required.")
		@Size(max = 50, message = "Mission code must be at most 50 characters.")
		String missionCode,
		@NotBlank(message = "Mission name is required.")
		@Size(max = 150, message = "Mission name must be at most 150 characters.")
		String missionName,
		@Size(max = 2000, message = "Description must be at most 2000 characters.")
		String description,
		@NotNull(message = "Start date is required.")
		LocalDate startDate,
		@NotNull(message = "End date is required.")
		LocalDate endDate,
		@NotNull(message = "Mission status is required.")
		MissionStatus status) {

		@AssertTrue(message = "End date must be on or after start date.")
		public boolean isDateRangeValid() {
			return startDate == null || endDate == null || !endDate.isBefore(startDate);
		}
	}
