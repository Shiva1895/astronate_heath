package com.astrahealth.backend.dto.response;

import com.astrahealth.backend.entity.Astronaut;

public record AstronautSummaryResponse(Long id, String astronautCode, String fullName, String status) {

	public static AstronautSummaryResponse from(Astronaut astronaut) {
		return new AstronautSummaryResponse(
				astronaut.getId(),
				astronaut.getAstronautCode(),
				astronaut.getFullName(),
				astronaut.getStatus());
	}
}
