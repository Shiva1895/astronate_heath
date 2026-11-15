package com.astrahealth.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.response.InAppNotificationResponse;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	public Page<InAppNotificationResponse> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@AuthenticationPrincipal Jwt jwt) {
		validatePage(page, size);
		return notificationService.list(
				userId(jwt),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
	}

	@GetMapping("/unread-count")
	public long unreadCount(@AuthenticationPrincipal Jwt jwt) {
		return notificationService.unreadCount(userId(jwt));
	}

	@PutMapping("/{id}/read")
	public InAppNotificationResponse markRead(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt) {
		return notificationService.markRead(id, userId(jwt));
	}

	@PutMapping("/read-all")
	public int markAllRead(@AuthenticationPrincipal Jwt jwt) {
		return notificationService.markAllRead(userId(jwt));
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
