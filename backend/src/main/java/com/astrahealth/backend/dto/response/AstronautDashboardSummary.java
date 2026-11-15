package com.astrahealth.backend.dto.response;

import java.time.Instant;
import java.util.List;

public record AstronautDashboardSummary(
		Long astronautId,
		String astronautCode,
		String fullName,
		String status,
		List<HealthReadingResponse> latestReadings,
		long activeAlertCount,
		Instant latestReadingAt) {
}
