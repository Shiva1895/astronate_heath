package com.astrahealth.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.response.AuditEventResponse;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.AuditService;

@RestController
@RequestMapping("/api/v1/audit-events")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

	private final AuditService auditService;

	public AuditController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping
	public Page<AuditEventResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
		return auditService.list(
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt")));
	}
}
