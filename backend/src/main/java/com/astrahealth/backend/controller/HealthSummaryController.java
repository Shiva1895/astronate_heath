package com.astrahealth.backend.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.response.HealthSummaryBucket;
import com.astrahealth.backend.dto.response.HealthSummaryComparison;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.service.HealthSummaryService;

@RestController
@RequestMapping("/api/v1/health/astronauts/{astronautId}/summaries")
public class HealthSummaryController {

	private final HealthSummaryService summaryService;

	public HealthSummaryController(HealthSummaryService summaryService) {
		this.summaryService = summaryService;
	}

	@GetMapping("/daily")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public List<HealthSummaryBucket> daily(
			@PathVariable Long astronautId,
			@RequestParam Instant from,
			@RequestParam Instant to,
			@RequestParam(required = false) ReadingType type) {
		return summaryService.daily(astronautId, from, to, type);
	}

	@GetMapping("/weekly")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public List<HealthSummaryBucket> weekly(
			@PathVariable Long astronautId,
			@RequestParam Instant from,
			@RequestParam Instant to,
			@RequestParam(required = false) ReadingType type) {
		return summaryService.weekly(astronautId, from, to, type);
	}

	@GetMapping("/compare")
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
}
