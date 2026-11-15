package com.astrahealth.backend.controller;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.astrahealth.backend.dto.request.CreateAstronautRequest;
import com.astrahealth.backend.dto.request.UpdateAstronautRequest;
import com.astrahealth.backend.dto.response.AstronautResponse;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.AstronautService;

@RestController
@RequestMapping("/api/v1/astronauts")
public class AstronautController {

	private static final int MAX_PAGE_SIZE = 100;
	private static final Set<String> SORTABLE_FIELDS =
			Set.of("id", "astronautCode", "fullName", "email", "status");

	private final AstronautService astronautService;

	public AstronautController(AstronautService astronautService) {
		this.astronautService = astronautService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AstronautResponse> createAstronaut(
			@Valid @RequestBody CreateAstronautRequest request) {
		Astronaut createdAstronaut = astronautService.createAstronaut(toAstronaut(
				request.getAstronautCode(),
				request.getFullName(),
				request.getEmail(),
				request.getStatus()));
		URI location = URI.create("/api/v1/astronauts/" + createdAstronaut.getId());

		return ResponseEntity.created(location).body(toResponse(createdAstronaut));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Page<AstronautResponse> getAllAstronauts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "astronautCode") String sortBy,
			@RequestParam(defaultValue = "ASC") String direction) {
		if (page < 0) {
			throw new InvalidRequestException("Page must be zero or greater.");
		}
		if (size < 1 || size > MAX_PAGE_SIZE) {
			throw new InvalidRequestException("Page size must be between 1 and 100.");
		}
		if (!SORTABLE_FIELDS.contains(sortBy)) {
			throw new InvalidRequestException("Unsupported sort field.");
		}

		Sort.Direction sortDirection = switch (direction.toUpperCase(Locale.ROOT)) {
			case "ASC" -> Sort.Direction.ASC;
			case "DESC" -> Sort.Direction.DESC;
			default -> throw new InvalidRequestException("Sort direction must be ASC or DESC.");
		};

		PageRequest pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
		return astronautService.getAllAstronauts(pageable).map(this::toResponse);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@astronautAccessService.canView(#p0, authentication)")
	public AstronautResponse getAstronautById(@PathVariable Long id) {
		validatePositiveAstronautId(id);
		return toResponse(astronautService.getAstronautById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AstronautResponse updateAstronaut(
			@PathVariable Long id,
			@Valid @RequestBody UpdateAstronautRequest request) {
		validatePositiveAstronautId(id);
		Astronaut updatedAstronaut = astronautService.updateAstronaut(
				id,
				toAstronaut(
						request.getAstronautCode(),
						request.getFullName(),
						request.getEmail(),
						request.getStatus()));
		return toResponse(updatedAstronaut);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteAstronaut(@PathVariable Long id) {
		validatePositiveAstronautId(id);
		astronautService.deleteAstronaut(id);
		return ResponseEntity.noContent().build();
	}

	private void validatePositiveAstronautId(Long id) {
		if (id <= 0) {
			throw new InvalidRequestException("Astronaut ID must be a positive number.");
		}
	}

	private Astronaut toAstronaut(String astronautCode, String fullName, String email, String status) {
		Astronaut astronaut = new Astronaut();
		astronaut.setAstronautCode(astronautCode);
		astronaut.setFullName(fullName);
		astronaut.setEmail(email);
		astronaut.setStatus(status);
		return astronaut;
	}

	private AstronautResponse toResponse(Astronaut astronaut) {
		return new AstronautResponse(
				astronaut.getId(),
				astronaut.getAstronautCode(),
				astronaut.getFullName(),
				astronaut.getEmail(),
				astronaut.getStatus());
	}
}
