package com.astrahealth.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.astrahealth.backend.entity.Mission;
import com.astrahealth.backend.entity.MissionStatus;

public record MissionResponse(
		Long id,
		String missionCode,
		String missionName,
		String description,
		LocalDate startDate,
		LocalDate endDate,
		MissionStatus status,
		boolean archived,
		Instant createdAt,
		Instant updatedAt,
		List<AstronautSummaryResponse> astronauts) {

	public static MissionResponse from(Mission mission) {
		return new MissionResponse(
				mission.getId(),
				mission.getMissionCode(),
				mission.getMissionName(),
				mission.getDescription(),
				mission.getStartDate(),
				mission.getEndDate(),
				mission.getStatus(),
				mission.getArchivedAt() != null,
				mission.getCreatedAt(),
				mission.getUpdatedAt(),
				mission.getAstronauts().stream().map(AstronautSummaryResponse::from).toList());
	}
}
