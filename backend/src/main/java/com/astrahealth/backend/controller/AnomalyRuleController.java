package com.astrahealth.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.request.AnomalyRuleRequest;
import com.astrahealth.backend.dto.response.AnomalyRuleResponse;
import com.astrahealth.backend.service.AnomalyRuleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/anomaly-rules")
@PreAuthorize("hasRole('ADMIN')")
public class AnomalyRuleController {

	private final AnomalyRuleService ruleService;

	public AnomalyRuleController(AnomalyRuleService ruleService) {
		this.ruleService = ruleService;
	}

	@GetMapping
	public List<AnomalyRuleResponse> list() {
		return ruleService.list();
	}

	@PostMapping
	public ResponseEntity<AnomalyRuleResponse> create(@Valid @RequestBody AnomalyRuleRequest request) {
		AnomalyRuleResponse response = ruleService.create(request);
		return ResponseEntity.created(URI.create("/api/v1/anomaly-rules/" + response.id())).body(response);
	}

	@PutMapping("/{id}")
	public AnomalyRuleResponse update(
			@PathVariable Long id,
			@Valid @RequestBody AnomalyRuleRequest request) {
		return ruleService.update(id, request);
	}
}
