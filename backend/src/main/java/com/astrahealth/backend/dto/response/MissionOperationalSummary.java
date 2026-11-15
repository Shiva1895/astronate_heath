package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.MissionStatus;

public record MissionOperationalSummary(
		Long missionId,
		String missionCode,
		String missionName,
		MissionStatus status,
		int astronautCount,
		long activeAlertCount,
		long offlineDeviceCount,
		Instant latestReadingAt) {
}
