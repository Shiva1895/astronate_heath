package com.astrahealth.backend.controller;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.entity.WellbeingReportStatus;
import com.astrahealth.backend.service.ReportExportService;
import com.astrahealth.backend.service.ReportFormat;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportExportController {

	private final ReportExportService exportService;

	public ReportExportController(ReportExportService exportService) {
		this.exportService = exportService;
	}

	@GetMapping("/astronauts/{astronautId}/health")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<byte[]> health(
			@PathVariable Long astronautId,
			@RequestParam Instant from,
			@RequestParam Instant to,
			@RequestParam(defaultValue = "CSV") ReportFormat format,
			@AuthenticationPrincipal Jwt jwt) {
		return response(exportService.health(astronautId, from, to, format, userId(jwt)));
	}

	@GetMapping("/missions/{missionId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MISSION_CONTROL')")
	public ResponseEntity<byte[]> mission(
			@PathVariable Long missionId,
			@RequestParam(defaultValue = "CSV") ReportFormat format,
			@AuthenticationPrincipal Jwt jwt) {
		return response(exportService.mission(missionId, format, userId(jwt)));
	}

	@GetMapping("/alerts")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<byte[]> alerts(
			@RequestParam(required = false) Long astronautId,
			@RequestParam(defaultValue = "CSV") ReportFormat format,
			@AuthenticationPrincipal Jwt jwt) {
		return response(exportService.alerts(astronautId, format, userId(jwt)));
	}

	@GetMapping("/astronauts/{astronautId}/follow-ups")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public ResponseEntity<byte[]> followUps(
			@PathVariable Long astronautId,
			@RequestParam(defaultValue = "CSV") ReportFormat format,
			@AuthenticationPrincipal Jwt jwt) {
		return response(exportService.followUps(astronautId, format, userId(jwt)));
	}

	@GetMapping("/astronauts/{astronautId}/wellbeing")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<byte[]> wellbeing(
			@PathVariable Long astronautId,
			@RequestParam(required = false) WellbeingReportStatus status,
			@RequestParam(defaultValue = "CSV") ReportFormat format,
			@AuthenticationPrincipal Jwt jwt) {
		return response(exportService.wellbeing(astronautId, status, format, userId(jwt)));
	}

	private ResponseEntity<byte[]> response(ReportExportService.ExportFile file) {
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(file.contentType()))
				.contentLength(file.contents().length)
				.cacheControl(CacheControl.noStore())
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + file.fileName() + "\"")
				.header("X-Content-Type-Options", "nosniff")
				.body(file.contents());
	}

	private Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
