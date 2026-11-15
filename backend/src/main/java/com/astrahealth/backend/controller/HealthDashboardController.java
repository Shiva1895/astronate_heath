package com.astrahealth.backend.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.response.AlertResponse;
import com.astrahealth.backend.dto.response.AstronautDashboardSummary;
import com.astrahealth.backend.dto.response.HealthReadingResponse;
import com.astrahealth.backend.dto.response.HealthSummaryBucket;
import com.astrahealth.backend.dto.response.HealthSummaryComparison;
import com.astrahealth.backend.dto.response.MissionOperationalSummary;
import com.astrahealth.backend.entity.MissionStatus;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.AlertService;
import com.astrahealth.backend.service.HealthDashboardService;
import com.astrahealth.backend.service.HealthReadingService;
import com.astrahealth.backend.service.HealthSummaryService;

@RestController
@RequestMapping("/api/v1/dashboard")
public class HealthDashboardController {

	private final HealthReadingService readingService;
	private final HealthSummaryService summaryService;
	private final AlertService alertService;
	private final HealthDashboardService dashboardService;

	public HealthDashboardController(
			HealthReadingService readingService,
			HealthSummaryService summaryService,
			AlertService alertService,
			HealthDashboardService dashboardService) {
		this.readingService = readingService;
		this.summaryService = summaryService;
		this.alertService = alertService;
		this.dashboardService = dashboardService;
	}

	@GetMapping("/astronauts/{astronautId}/latest-readings")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public List<HealthReadingResponse> latestReadings(
			@PathVariable Long astronautId,
			@RequestParam(required = false) ReadingType type,
			@AuthenticationPrincipal Jwt jwt) {
		return readingService.latest(astronautId, type, Long.valueOf(jwt.getSubject()));
	}

	@GetMapping("/astronauts/{astronautId}/active-alerts")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public List<AlertResponse> activeAlerts(
			@PathVariable Long astronautId,
			@AuthenticationPrincipal Jwt jwt) {
		return alertService.activeForAstronaut(astronautId, Long.valueOf(jwt.getSubject()));
	}

	@GetMapping("/astronauts/{astronautId}/health-summary")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public List<HealthSummaryBucket> healthSummary(
			@PathVariable Long astronautId,
			@RequestParam Instant from,
			@RequestParam Instant to,
			@RequestParam(defaultValue = "DAY") String bucket,
			@RequestParam(required = false) ReadingType type) {
		return switch (bucket.toUpperCase(java.util.Locale.ROOT)) {
			case "DAY" -> summaryService.daily(astronautId, from, to, type);
			case "WEEK" -> summaryService.weekly(astronautId, from, to, type);
			default -> throw new InvalidRequestException("Bucket must be DAY or WEEK.");
		};
	}

	@GetMapping("/astronauts/{astronautId}/trends")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public List<HealthSummaryBucket> trends(
			@PathVariable Long astronautId,
			@RequestParam Instant from,
			@RequestParam Instant to,
			@RequestParam ReadingType type) {
		return summaryService.daily(astronautId, from, to, type);
	}

	@GetMapping("/astronauts/{astronautId}/health-comparison")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public HealthSummaryComparison compare(
			@PathVariable Long astronautId,
			@RequestParam Instant firstFrom,
			@RequestParam Instant firstTo,
			@RequestParam Instant secondFrom,
			@RequestParam Instant secondTo,
			@RequestParam ReadingType type) {
		return summaryService.compare(
				astronautId,
				type,
				firstFrom,
				firstTo,
				secondFrom,
				secondTo);
	}

	@GetMapping("/medical-officer/astronauts")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public Page<AstronautDashboardSummary> medicalOfficerAstronauts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
		return dashboardService.astronautSummaries(Long.valueOf(jwt.getSubject()), pageable);
	}

	@GetMapping("/missions")
	@PreAuthorize("hasAnyRole('ADMIN', 'MISSION_CONTROL')")
	public Page<MissionOperationalSummary> missions(
			@RequestParam(required = false) MissionStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		validatePage(page, size);
		return dashboardService.missionSummaries(
				status,
				PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "missionCode")));
	}

	@GetMapping("/missions/{missionId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MISSION_CONTROL')")
	public MissionOperationalSummary mission(@PathVariable Long missionId) {
		return dashboardService.missionSummary(missionId);
	}

	private void validatePage(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
	}
}
