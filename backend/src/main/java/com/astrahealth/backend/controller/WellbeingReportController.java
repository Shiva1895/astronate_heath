package com.astrahealth.backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.astrahealth.backend.dto.request.WellbeingReportRequest;
import com.astrahealth.backend.dto.request.WellbeingReviewRequest;
import com.astrahealth.backend.dto.response.WellbeingAudioResponse;
import com.astrahealth.backend.dto.response.WellbeingReportResponse;
import com.astrahealth.backend.entity.WellbeingReportStatus;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.WellbeingReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/wellbeing/reports")
public class WellbeingReportController {

	private final WellbeingReportService reportService;

	public WellbeingReportController(WellbeingReportService reportService) {
		this.reportService = reportService;
	}

	@PostMapping("/audio-drafts")
	@PreAuthorize("isAuthenticated()")
	public WellbeingAudioResponse uploadDraft(
			@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal Jwt jwt) {
		return reportService.uploadDraft(file, userId(jwt));
	}

	@GetMapping("/audio-drafts/{audioId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<byte[]> previewDraft(
			@PathVariable Long audioId,
			@AuthenticationPrincipal Jwt jwt) {
		WellbeingReportService.PrivateAudio audio = reportService.downloadDraft(audioId, userId(jwt));
		return audioResponse(audio);
	}

	@DeleteMapping("/audio-drafts/{audioId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> deleteDraft(
			@PathVariable Long audioId,
			@AuthenticationPrincipal Jwt jwt) {
		reportService.deleteDraft(audioId, userId(jwt));
		return ResponseEntity.noContent().build();
	}

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<WellbeingReportResponse> submit(
			@Valid @RequestBody WellbeingReportRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		WellbeingReportResponse response = reportService.submit(request, userId(jwt));
		return ResponseEntity.created(
				java.net.URI.create("/api/v1/wellbeing/reports/" + response.id()))
				.body(response);
	}

	@GetMapping("/mine")
	@PreAuthorize("isAuthenticated()")
	public Page<WellbeingReportResponse> mine(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return reportService.myReports(
				userId(jwt),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt")));
	}

	@GetMapping("/inbox")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public Page<WellbeingReportResponse> inbox(
			@RequestParam(required = false) WellbeingReportStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return reportService.inbox(
				userId(jwt),
				status,
				PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "submittedAt")));
	}

	@GetMapping("/{reportId}")
	@PreAuthorize("isAuthenticated()")
	public WellbeingReportResponse get(
			@PathVariable Long reportId,
			@AuthenticationPrincipal Jwt jwt) {
		return reportService.get(reportId, userId(jwt));
	}

	@PutMapping("/{reportId}")
	@PreAuthorize("isAuthenticated()")
	public WellbeingReportResponse edit(
			@PathVariable Long reportId,
			@Valid @RequestBody WellbeingReportRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return reportService.edit(reportId, request, userId(jwt));
	}

	@PutMapping("/{reportId}/review")
	@PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_OFFICER')")
	public WellbeingReportResponse review(
			@PathVariable Long reportId,
			@Valid @RequestBody WellbeingReviewRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return reportService.review(reportId, request, userId(jwt));
	}

	@GetMapping("/{reportId}/audio")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<byte[]> audio(
			@PathVariable Long reportId,
			@AuthenticationPrincipal Jwt jwt) {
		return audioResponse(reportService.downloadReportAudio(reportId, userId(jwt)));
	}

	private ResponseEntity<byte[]> audioResponse(WellbeingReportService.PrivateAudio audio) {
		MediaType mediaType = MediaType.parseMediaType(audio.mediaType());
		return ResponseEntity.ok()
				.contentType(mediaType)
				.contentLength(audio.bytes().length)
				.cacheControl(CacheControl.noStore())
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"inline; filename=\"" + UUID.randomUUID() + "\"")
				.header("X-Content-Type-Options", "nosniff")
				.body(audio.bytes());
	}

	private Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}

	private void validatePage(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
	}
}
