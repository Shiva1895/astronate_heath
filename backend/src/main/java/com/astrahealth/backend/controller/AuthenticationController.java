package com.astrahealth.backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.request.LoginRequest;
import com.astrahealth.backend.dto.request.RefreshTokenRequest;
import com.astrahealth.backend.dto.request.RegisterRequest;
import com.astrahealth.backend.dto.response.AuthTokenResponse;
import com.astrahealth.backend.dto.response.RegistrationResponse;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/register")
	public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
	}

	@PostMapping("/login")
	public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authenticationService.login(request);
	}

	@PostMapping("/refresh")
	public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authenticationService.refresh(request);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody RefreshTokenRequest request) {
		authenticationService.logout(Long.valueOf(jwt.getSubject()), request.getRefreshToken());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/admin/accounts/{id}/activate")
	public ResponseEntity<Void> activateAccount(@PathVariable Long id) {
		validatePositiveAccountId(id);
		authenticationService.activateAccount(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/admin/accounts/{id}/deactivate")
	public ResponseEntity<Void> deactivateAccount(@PathVariable Long id) {
		validatePositiveAccountId(id);
		authenticationService.deactivateAccount(id);
		return ResponseEntity.noContent().build();
	}

	private void validatePositiveAccountId(Long id) {
		if (id <= 0) {
			throw new InvalidRequestException("Account ID must be a positive number.");
		}
	}
}
