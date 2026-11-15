package com.astrahealth.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.request.CreateAlertRequest;
import com.astrahealth.backend.dto.response.AlertHistoryResponse;
import com.astrahealth.backend.dto.response.AlertResponse;
import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AlertStatus;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.AlertService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

	private final AlertService alertService;

	public AlertController(AlertService alertService) {
		this.alertService = alertService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public ResponseEntity<AlertResponse> create(
			@Valid @RequestBody CreateAlertRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		AlertResponse response = alertService.createManual(request, Long.valueOf(jwt.getSubject()));
		return ResponseEntity.created(java.net.URI.create("/api/v1/alerts/" + response.id())).body(response);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Page<AlertResponse> listAll(
			@RequestParam(required = false) AlertStatus status,
			@RequestParam(required = false) AlertPriority priority,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		validatePage(page, size);
		return alertService.listAll(
				status,
				priority,
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
	}

	@GetMapping("/astronauts/{astronautId}")
	@PreAuthorize("@healthAccessService.canAccessAstronaut(#p0, authentication)")
	public Page<AlertResponse> listForAstronaut(
			@PathVariable Long astronautId,
			@RequestParam(required = false) AlertStatus status,
			@RequestParam(required = false) AlertPriority priority,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return alertService.listForAstronaut(
				astronautId,
				status,
				priority,
				Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public AlertResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return alertService.get(id, Long.valueOf(jwt.getSubject()));
	}

	@GetMapping("/{id}/history")
	@PreAuthorize("isAuthenticated()")
	public Page<AlertHistoryResponse> history(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return alertService.history(
				id,
				Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt")));
	}

	@PutMapping("/{id}/acknowledge")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public AlertResponse acknowledge(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return alertService.acknowledge(id, Long.valueOf(jwt.getSubject()));
	}

	@PutMapping("/{id}/escalate")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public AlertResponse escalate(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return alertService.escalate(id, Long.valueOf(jwt.getSubject()));
	}

	@PutMapping("/{id}/resolve")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public AlertResponse resolve(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return alertService.resolve(id, Long.valueOf(jwt.getSubject()));
	}

	@PutMapping("/{id}/close")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public AlertResponse close(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return alertService.close(id, Long.valueOf(jwt.getSubject()));
	}

	private void validatePage(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
	}
}
