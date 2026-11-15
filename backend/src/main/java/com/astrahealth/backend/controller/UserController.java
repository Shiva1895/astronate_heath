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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.request.LinkAstronautProfileRequest;
import com.astrahealth.backend.dto.request.UpdateUserProfileRequest;
import com.astrahealth.backend.dto.request.UpdateUserRolesRequest;
import com.astrahealth.backend.dto.response.UserResponse;
import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.service.UserManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private static final int MAX_PAGE_SIZE = 100;
	private static final java.util.Set<String> SORT_FIELDS =
			java.util.Set.of("id", "email", "username", "displayName", "status", "createdAt");
	private final UserManagementService userManagementService;

	public UserController(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public UserResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
		return userManagementService.getCurrentUser(Long.valueOf(jwt.getSubject()));
	}

	@PutMapping("/me/profile")
	@PreAuthorize("isAuthenticated()")
	public UserResponse updateCurrentProfile(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateUserProfileRequest request) {
		return userManagementService.updateProfile(Long.valueOf(jwt.getSubject()), request);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Page<UserResponse> searchUsers(
			@RequestParam(required = false) String query,
			@RequestParam(required = false) AccountStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "email") String sortBy,
			@RequestParam(defaultValue = "ASC") String direction) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE || !SORT_FIELDS.contains(sortBy)) {
			throw new com.astrahealth.backend.exception.InvalidRequestException(
					"User pagination or sort parameter is invalid.");
		}
		Sort.Direction sortDirection;
		try {
			sortDirection = Sort.Direction.fromString(direction);
		} catch (IllegalArgumentException exception) {
			throw new com.astrahealth.backend.exception.InvalidRequestException(
					"Sort direction must be ASC or DESC.");
		}
		return userManagementService.searchUsers(
				query,
				status,
				PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse getUser(@PathVariable Long id) {
		return userManagementService.getUser(id);
	}

	@PutMapping("/{id}/profile")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse updateProfile(
			@PathVariable Long id,
			@Valid @RequestBody UpdateUserProfileRequest request) {
		return userManagementService.updateProfile(id, request);
	}

	@PutMapping("/{id}/roles")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse updateRoles(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateUserRolesRequest request) {
		return userManagementService.updateRoles(id, Long.valueOf(jwt.getSubject()), request.roles());
	}

	@PutMapping("/{id}/astronaut-profile")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse linkAstronautProfile(
			@PathVariable Long id,
			@Valid @RequestBody LinkAstronautProfileRequest request) {
		return userManagementService.linkAstronautProfile(id, request.astronautId());
	}

	@DeleteMapping("/{id}/astronaut-profile")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse unlinkAstronautProfile(@PathVariable Long id) {
		return userManagementService.unlinkAstronautProfile(id);
	}

	@PutMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> activateAccount(@PathVariable Long id) {
		userManagementService.activateAccount(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deactivateAccount(@PathVariable Long id) {
		userManagementService.deactivateAccount(id);
		return ResponseEntity.noContent().build();
	}
}
