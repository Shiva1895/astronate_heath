package com.astrahealth.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Marks this class as a REST controller whose method results are HTTP responses.
@RestController
// Provides a shared URL prefix for this controller's endpoints.
@RequestMapping("/api/v1")
public class HealthController {

	// Maps HTTP GET requests to /api/v1/health.
	@GetMapping("/health")
	public String getHealthStatus() {
		return "ASTRA HEALTH Backend is running successfully!";
	}
}
