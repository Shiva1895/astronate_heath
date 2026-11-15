package com.astrahealth.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.request.MissionRequest;
import com.astrahealth.backend.dto.response.MissionResponse;
import com.astrahealth.backend.entity.MissionStatus;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.MissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/missions")
public class MissionController {

	private static final int MAX_PAGE_SIZE = 100;
	private static final java.util.Set<String> SORT_FIELDS =
			java.util.Set.of("id", "missionCode", "missionName", "startDate", "endDate", "status");
	private final MissionService missionService;

	public MissionController(MissionService missionService) {
		this.missionService = missionService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public MissionResponse create(@Valid @RequestBody MissionRequest request) {
		return missionService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public MissionResponse update(@PathVariable Long id, @Valid @RequestBody MissionRequest request) {
		return missionService.update(id, request);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MISSION_CONTROL')")
	public MissionResponse get(@PathVariable Long id) {
		return missionService.get(id);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'MISSION_CONTROL')")
	public Page<MissionResponse> search(
			@RequestParam(required = false) String query,
			@RequestParam(required = false) MissionStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "missionCode") String sortBy,
			@RequestParam(defaultValue = "ASC") String direction) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE || !SORT_FIELDS.contains(sortBy)) {
			throw new InvalidRequestException("Mission pagination or sort parameter is invalid.");
		}
		Sort.Direction sortDirection;
		try {
			sortDirection = Sort.Direction.fromString(direction);
		} catch (IllegalArgumentException exception) {
			throw new InvalidRequestException("Sort direction must be ASC or DESC.");
		}
		return missionService.search(
				query,
				status,
				PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
	}

	@PutMapping("/{missionId}/astronauts/{astronautId}")
	@PreAuthorize("hasRole('ADMIN')")
	public MissionResponse assignAstronaut(
			@PathVariable Long missionId,
			@PathVariable Long astronautId) {
		return missionService.assignAstronaut(missionId, astronautId);
	}

	@DeleteMapping("/{missionId}/astronauts/{astronautId}")
	@PreAuthorize("hasRole('ADMIN')")
	public MissionResponse removeAstronaut(
			@PathVariable Long missionId,
			@PathVariable Long astronautId) {
		return missionService.removeAstronaut(missionId, astronautId);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> archive(@PathVariable Long id) {
		missionService.archive(id);
		return ResponseEntity.noContent().build();
	}
}
