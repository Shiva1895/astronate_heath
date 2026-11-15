package com.astrahealth.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.astrahealth.backend.dto.request.LoginRequest;
import com.astrahealth.backend.dto.request.RegisterRequest;
import com.astrahealth.backend.dto.response.AuthTokenResponse;
import com.astrahealth.backend.dto.response.RegistrationResponse;
import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.GlobalExceptionHandler;
import com.astrahealth.backend.exception.InvalidCredentialsException;
import com.astrahealth.backend.service.AuthenticationService;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTests {

	@Mock
	private AuthenticationService authenticationService;

	@InjectMocks
	private AuthenticationController authenticationController;

	private LocalValidatorFactoryBean validator;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() throws Exception {
		validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@AfterEach
	void tearDown() {
		validator.close();
	}

	@Test
	void registerReturnsPendingAstronautAccountWithoutPasswordFields() throws Exception {
		when(authenticationService.register(any(RegisterRequest.class)))
				.thenReturn(new RegistrationResponse(
						7L,
						"astronaut@example.com",
						AccountStatus.PENDING_ACTIVATION,
						Set.of(UserRole.ASTRONAUT)));

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRegistrationRequest()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.email").value("astronaut@example.com"))
				.andExpect(jsonPath("$.status").value("PENDING_ACTIVATION"))
				.andExpect(jsonPath("$.roles[0]").value("ASTRONAUT"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void registrationRejectsInvalidPassword() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"astronaut@example.com","password":"short"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.validationErrors.password").exists());
	}

	@Test
	void registrationRejectsInvalidEmail() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"not-an-email","password":"A-Long-Unique-Password-123!"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.validationErrors.email").exists());
	}

	@Test
	void duplicateRegistrationUsesSharedConflictResponse() throws Exception {
		when(authenticationService.register(any(RegisterRequest.class)))
				.thenThrow(new DuplicateResourceException("An account with the provided email already exists."));

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRegistrationRequest()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void loginReturnsBearerAccessAndRefreshTokens() throws Exception {
		when(authenticationService.login(any(LoginRequest.class)))
				.thenReturn(new AuthTokenResponse("access-token", 900L, "refresh-token"));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"astronaut@example.com","password":"A-Long-Unique-Password-123!"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("access-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(900))
				.andExpect(jsonPath("$.refreshToken").value("refresh-token"));
	}

	@Test
	void invalidLoginReturnsGenericUnauthorizedResponse() throws Exception {
		when(authenticationService.login(any(LoginRequest.class)))
				.thenThrow(new InvalidCredentialsException());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"astronaut@example.com","password":"A-Long-Unique-Password-123!"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid credentials or inactive account."));
	}

	@Test
	void refreshReturnsRotatedTokens() throws Exception {
		when(authenticationService.refresh(any()))
				.thenReturn(new AuthTokenResponse("new-access-token", 900L, "new-refresh-token"));

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refreshToken":"old-refresh-token"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("new-access-token"))
				.andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

		verify(authenticationService).refresh(any());
	}

	private String validRegistrationRequest() {
		return """
				{"email":"astronaut@example.com","password":"A-Long-Unique-Password-123!"}
				""";
	}
}
