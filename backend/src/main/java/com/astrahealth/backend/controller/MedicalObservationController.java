package com.astrahealth.backend.controller;

import java.net.URI;
import java.util.List;

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

import com.astrahealth.backend.dto.request.FollowUpStatusRequest;
import com.astrahealth.backend.dto.request.MedicalFollowUpRequest;
import com.astrahealth.backend.dto.request.MedicalObservationRequest;
import com.astrahealth.backend.dto.response.MedicalFollowUpResponse;
import com.astrahealth.backend.dto.response.MedicalObservationHistoryResponse;
import com.astrahealth.backend.dto.response.MedicalObservationResponse;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.MedicalObservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/medical-observations")
@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
public class MedicalObservationController {

	private final MedicalObservationService observationService;

	public MedicalObservationController(MedicalObservationService observationService) {
		this.observationService = observationService;
	}

	@GetMapping("/follow-ups/inbox")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public Page<MedicalFollowUpResponse> followUpInbox(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return observationService.followUpsForAssignedAstronauts(
				Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size));
	}

	@PostMapping
	public ResponseEntity<MedicalObservationResponse> create(
			@Valid @RequestBody MedicalObservationRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		MedicalObservationResponse response = observationService.create(request, Long.valueOf(jwt.getSubject()));
		return ResponseEntity.created(URI.create("/api/v1/medical-observations/" + response.id())).body(response);
	}

	@GetMapping("/astronauts/{astronautId}")
	public Page<MedicalObservationResponse> list(
			@PathVariable Long astronautId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return observationService.listByAstronaut(
				astronautId,
				Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
	}

	@GetMapping("/{id}")
	public MedicalObservationResponse get(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt) {
		return observationService.get(id, Long.valueOf(jwt.getSubject()));
	}

	@PutMapping("/{id}")
	public MedicalObservationResponse update(
			@PathVariable Long id,
			@Valid @RequestBody MedicalObservationRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return observationService.update(id, request, Long.valueOf(jwt.getSubject()));
	}

	@GetMapping("/{id}/history")
	public Page<MedicalObservationHistoryResponse> history(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return observationService.history(
				id,
				Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt")));
	}

	@PostMapping("/{id}/follow-ups")
	public MedicalFollowUpResponse addFollowUp(
			@PathVariable Long id,
			@Valid @RequestBody MedicalFollowUpRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return observationService.addFollowUp(id, request, Long.valueOf(jwt.getSubject()));
	}

	@GetMapping("/{id}/follow-ups")
	public List<MedicalFollowUpResponse> listFollowUps(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt) {
		return observationService.listFollowUps(id, Long.valueOf(jwt.getSubject()));
	}

	@PutMapping("/{id}/follow-ups/{followUpId}/status")
	public MedicalFollowUpResponse updateFollowUpStatus(
			@PathVariable Long id,
			@PathVariable Long followUpId,
			@Valid @RequestBody FollowUpStatusRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return observationService.updateFollowUpStatus(
				id,
				followUpId,
				request.status(),
				Long.valueOf(jwt.getSubject()));
	}

	private void validatePage(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
	}
}
