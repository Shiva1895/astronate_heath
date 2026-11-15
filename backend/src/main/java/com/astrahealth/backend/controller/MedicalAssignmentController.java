package com.astrahealth.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.request.CreateMedicalAssignmentRequest;
import com.astrahealth.backend.dto.response.AstronautResponse;
import com.astrahealth.backend.dto.response.MedicalAssignmentResponse;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.MedicalAssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/medical-assignments")
public class MedicalAssignmentController {

	private final MedicalAssignmentService assignmentService;

	public MedicalAssignmentController(MedicalAssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public MedicalAssignmentResponse assign(@Valid @RequestBody CreateMedicalAssignmentRequest request) {
		return assignmentService.assign(request.medicalOfficerUserId(), request.astronautId());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> remove(@PathVariable Long id) {
		assignmentService.remove(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Page<MedicalAssignmentResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
		return assignmentService.listAssignments(
				PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")));
	}

	@GetMapping("/me/astronauts")
	@PreAuthorize("hasRole('MEDICAL_OFFICER')")
	public Page<AstronautResponse> getAssignedAstronauts(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
		return assignmentService.getAssignedAstronauts(
				Long.valueOf(jwt.getSubject()),
				PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
	}
}
