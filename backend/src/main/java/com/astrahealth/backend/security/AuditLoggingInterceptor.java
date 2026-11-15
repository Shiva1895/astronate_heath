package com.astrahealth.backend.security;

import java.util.Arrays;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.astrahealth.backend.service.AuditService;

@Component
public class AuditLoggingInterceptor implements HandlerInterceptor {

	private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
	private final AuditService auditService;

	public AuditLoggingInterceptor(AuditService auditService) {
		this.auditService = auditService;
	}

	@Override
	public void afterCompletion(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			Exception exception) {
		String path = request.getRequestURI();
		String method = request.getMethod();
		if (exception != null || response.getStatus() >= 400 || !shouldAudit(method, path)) {
			return;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Long actorId = parseActorId(authentication);
		String[] segments = path.split("/");
		Long resourceId = Arrays.stream(segments)
				.filter(segment -> segment.matches("[0-9]+"))
				.map(Long::valueOf)
				.findFirst()
				.orElse(null);
		String resourceType = resourceType(path);
		String action = action(method, path);
		auditService.record(actorId, action, resourceType, resourceId, "method=" + method);
	}

	private boolean shouldAudit(String method, String path) {
		if (path.equals("/api/v1/auth/register")
				|| path.equals("/api/v1/auth/login")
				|| path.equals("/api/v1/auth/logout")) {
			return false;
		}
		if (path.matches("/api/v1/wellbeing/reports/[0-9]+/audio")
				|| path.matches("/api/v1/wellbeing/reports/audio-drafts/[0-9]+")) {
			return "GET".equals(method);
		}
		return WRITE_METHODS.contains(method) && path.startsWith("/api/v1/");
	}

	private Long parseActorId(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication.getName() == null
				|| !authentication.getName().matches("[0-9]+")) {
			return null;
		}
		return Long.valueOf(authentication.getName());
	}

	private String resourceType(String path) {
		if (path.startsWith("/api/v1/auth/")) return "authentication";
		if (path.startsWith("/api/v1/wellbeing/")) return "wellbeing";
		String[] segments = path.split("/");
		return segments.length > 3 ? segments[3].replace('-', '_') : "api_resource";
	}

	private String action(String method, String path) {
		if (path.equals("/api/v1/auth/register")) return "USER_CREATED";
		if (path.equals("/api/v1/auth/logout")) return "LOGOUT";
		if (path.endsWith("/roles")) return "ROLE_CHANGED";
		if (path.endsWith("/assign")) return "DEVICE_ASSIGNED";
		if (path.endsWith("/unassign")) return "DEVICE_UNASSIGNED";
		if (path.contains("/readings")) return "READING_CREATED";
		if (path.endsWith("/acknowledge")) return "ALERT_ACKNOWLEDGED";
		if (path.endsWith("/resolve")) return "ALERT_RESOLVED";
		if (path.matches("/api/v1/wellbeing/reports/[0-9]+/audio")
				|| path.matches("/api/v1/wellbeing/reports/audio-drafts/[0-9]+")) return "AUDIO_ACCESSED";
		if (path.equals("/api/v1/wellbeing/reports")) return "WELLBEING_REPORT_SUBMITTED";
		if (path.contains("/wellbeing/reports/")) return "WELLBEING_REPORT_UPDATED";
		if (path.contains("/medical-observations")) return "MEDICAL_OBSERVATION_CHANGED";
		if (path.contains("/missions")) return "MISSION_CHANGED";
		if (path.contains("/devices")) return "DEVICE_CHANGED";
		if (path.contains("/alerts")) return "ALERT_CHANGED";
		return method + "_REQUEST";
	}
}
