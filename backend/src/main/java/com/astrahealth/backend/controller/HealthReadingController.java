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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.request.CreateHealthReadingRequest;
import com.astrahealth.backend.dto.response.HealthReadingResponse;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.HealthReadingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/health")
public class HealthReadingController {

	private final HealthReadingService readingService;

	public HealthReadingController(HealthReadingService readingService) {
		this.readingService = readingService;
	}

	@PostMapping("/readings")
	@PreAuthorize("isAuthenticated()")
	public HealthReadingResponse create(
			@Valid @RequestBody CreateHealthReadingRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return readingService.create(request, Long.valueOf(jwt.getSubject()));
	}

	@PostMapping("/readings/bulk")
	@PreAuthorize("isAuthenticated()")
	public List<HealthReadingResponse> ingest(
			@Valid @RequestBody List<@Valid CreateHealthReadingRequest> requests,
			@AuthenticationPrincipal Jwt jwt) {
		return readingService.ingest(requests, Long.valueOf(jwt.getSubject()));
	}

	@GetMapping("/readings/{id}")
	@PreAuthorize("isAuthenticated()")
	public HealthReadingResponse get(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt) {
		return readingService.get(id, Long.valueOf(jwt.getSubject()));
	}

	@GetMapping("/astronauts/{astronautId}/readings")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public Page<HealthReadingResponse> history(
			@PathVariable Long astronautId,
			@RequestParam(required = false) ReadingType type,
			@RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return readingService.history(
				astronautId,
				type,
				from,
				to,
				Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedAt").and(
						Sort.by(Sort.Direction.DESC, "id"))));
	}

	@GetMapping("/astronauts/{astronautId}/readings/latest")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public List<HealthReadingResponse> latest(
			@PathVariable Long astronautId,
			@RequestParam(required = false) ReadingType type,
			@AuthenticationPrincipal Jwt jwt) {
		return readingService.latest(astronautId, type, Long.valueOf(jwt.getSubject()));
	}

	private void validatePage(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
	}
}
