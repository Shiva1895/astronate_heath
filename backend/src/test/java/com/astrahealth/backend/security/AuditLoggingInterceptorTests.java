package com.astrahealth.backend.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.astrahealth.backend.service.AuditService;

class AuditLoggingInterceptorTests {

	private final AuditService auditService = mock(AuditService.class);
	private final AuditLoggingInterceptor interceptor = new AuditLoggingInterceptor(auditService);

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void successfulAuthorizedAudioPlaybackRecordsActorAndResourceWithoutContent() {
		SecurityContextHolder.getContext().setAuthentication(
				UsernamePasswordAuthenticationToken.authenticated("42", null, List.of()));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/api/v1/wellbeing/reports/123/audio");
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.setStatus(200);

		interceptor.afterCompletion(request, response, null, null);

		verify(auditService).record(42L, "AUDIO_ACCESSED", "wellbeing", 123L, "method=GET");
	}

	@Test
	void unsuccessfulRequestsAreNotRecordedAsCompletedActions() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/api/v1/wellbeing/reports/123/audio");
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.setStatus(403);

		interceptor.afterCompletion(request, response, null, null);

		verifyNoInteractions(auditService);
	}
}
