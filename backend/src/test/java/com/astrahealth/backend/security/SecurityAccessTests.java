package com.astrahealth.backend.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.MedicalAssignment;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.entity.HealthDataSource;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.entity.MedicalObservation;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.MedicalObservationRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@ActiveProfiles("test")
@SpringBootTest(properties = "astra.security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@AutoConfigureMockMvc
class SecurityAccessTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private AstronautRepository astronautRepository;

	@Autowired
	private MedicalAssignmentRepository assignmentRepository;

	@Autowired
	private HealthReadingRepository healthReadingRepository;

	@Autowired
	private MedicalObservationRepository medicalObservationRepository;

	@Test
	void healthEndpointRemainsPublic() throws Exception {
		mockMvc.perform(get("/api/v1/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("ASTRA HEALTH Backend is running successfully!"));
	}

	@Test
	void corsAllowsConfiguredLocalFrontendAndRejectsOtherOrigins() throws Exception {
		mockMvc.perform(options("/api/v1/auth/login")
						.header("Origin", "http://localhost:5173")
						.header("Access-Control-Request-Method", "POST")
						.header("Access-Control-Request-Headers", "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

		mockMvc.perform(options("/api/v1/auth/login")
						.header("Origin", "https://untrusted.example")
						.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}

	@Test
	void openApiDocumentsBearerSecurityWhileLeavingLoginPublic() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("ASTRA HEALTH API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.paths['/api/v1/astronauts'].get.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").doesNotExist());
	}

	@Test
	void astronautApiRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/astronauts"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void privateWellbeingAndAudioEndpointsRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/wellbeing/reports/mine"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/v1/wellbeing/reports/1/audio"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/v1/notifications"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void missionControlCannotAccessMedicalInboxOrAuditEvents() throws Exception {
		mockMvc.perform(get("/api/v1/wellbeing/reports/inbox")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MISSION_CONTROL"))))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/audit-events")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MISSION_CONTROL"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void astronautRoleCannotListAllAstronauts() throws Exception {
		mockMvc.perform(get("/api/v1/astronauts")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ASTRONAUT"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void invalidBearerTokenReturnsGenericUnauthorizedResponse() throws Exception {
		mockMvc.perform(get("/api/v1/astronauts")
						.header("Authorization", "Bearer invalid-token"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void expiredSignedBearerTokenReturnsUnauthorized() throws Exception {
		Instant now = Instant.now();
		String token = createSignedToken(99L, now.minusSeconds(120), now.minusSeconds(60));

		mockMvc.perform(get("/api/v1/astronauts")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value(
						"Authentication is required or credentials are invalid."));
	}

	@Test
	@Transactional
	void validSignedBearerTokenAllowsProtectedEndpointAccess() throws Exception {
		UserAccount account = new UserAccount();
		account.setEmail("security-test-" + UUID.randomUUID() + "@example.com");
		account.setPasswordHash("test-password-hash");
		account.setRoles(EnumSet.of(UserRole.ASTRONAUT));
		account.setStatus(AccountStatus.ACTIVE);
		account = userAccountRepository.saveAndFlush(account);

		Instant now = Instant.now();
		String token = createSignedToken(account.getId(), now.minusSeconds(1), now.plusSeconds(60));

		mockMvc.perform(get("/api/v1/users/me")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(account.getId()))
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	@Transactional
	void astronautCanReadOwnProfileButNotAnotherAstronaut() throws Exception {
		Astronaut ownProfile = astronaut("AST-OWN");
		Astronaut otherProfile = astronaut("AST-OTHER");
		astronautRepository.saveAndFlush(ownProfile);
		astronautRepository.saveAndFlush(otherProfile);

		UserAccount account = account("astronaut-" + UUID.randomUUID() + "@example.com", UserRole.ASTRONAUT);
		account.setAstronautProfile(ownProfile);
		account = userAccountRepository.saveAndFlush(account);
		String token = createSignedToken(
				account.getId(),
				Instant.now().minusSeconds(1),
				Instant.now().plusSeconds(60));

		mockMvc.perform(get("/api/v1/astronauts/" + ownProfile.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/astronauts/" + otherProfile.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	@Transactional
	void medicalOfficerCanReadOnlyAssignedAstronauts() throws Exception {
		Astronaut assigned = astronaut("AST-ASSIGNED");
		Astronaut unassigned = astronaut("AST-UNASSIGNED");
		astronautRepository.saveAndFlush(assigned);
		astronautRepository.saveAndFlush(unassigned);
		UserAccount officer = userAccountRepository.saveAndFlush(
				account("medical-" + UUID.randomUUID() + "@example.com", UserRole.MEDICAL_OFFICER));
		MedicalAssignment assignment = new MedicalAssignment();
		assignment.setAstronaut(assigned);
		assignment.setMedicalOfficer(officer);
		assignmentRepository.saveAndFlush(assignment);
		HealthReading reading = new HealthReading();
		reading.setAstronaut(assigned);
		reading.setReadingType(ReadingType.HEART_RATE);
		reading.setValue(new java.math.BigDecimal("72"));
		reading.setUnit("bpm");
		reading.setRecordedAt(Instant.now());
		reading.setDataSource(HealthDataSource.MANUAL);
		healthReadingRepository.saveAndFlush(reading);
		MedicalObservation observation = new MedicalObservation();
		observation.setAstronaut(assigned);
		observation.setMedicalOfficer(officer);
		observation.setObservationText("Private test observation");
		observation = medicalObservationRepository.saveAndFlush(observation);
		String token = createSignedToken(
				officer.getId(),
				Instant.now().minusSeconds(1),
				Instant.now().plusSeconds(60));

		mockMvc.perform(get("/api/v1/astronauts/" + assigned.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/astronauts/" + unassigned.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/health/astronauts/" + assigned.getId() + "/readings")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].value").value(72));
		mockMvc.perform(get("/api/v1/health/astronauts/" + unassigned.getId() + "/readings")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/medical-observations/" + observation.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.observationText").value("Private test observation"));

		UserAccount otherOfficer = userAccountRepository.saveAndFlush(
				account("medical-other-" + UUID.randomUUID() + "@example.com", UserRole.MEDICAL_OFFICER));
		String otherToken = createSignedToken(
				otherOfficer.getId(),
				Instant.now().minusSeconds(1),
				Instant.now().plusSeconds(60));
		mockMvc.perform(get("/api/v1/medical-observations/" + observation.getId())
						.header("Authorization", "Bearer " + otherToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void missionControlCanReadMissionAndDeviceSummaries() throws Exception {
		mockMvc.perform(get("/api/v1/missions")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MISSION_CONTROL"))))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/devices")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MISSION_CONTROL"))))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/dashboard/missions")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MISSION_CONTROL"))))
				.andExpect(status().isOk());
	}

	@Test
	void missionControlCannotReadRawHealthDataOrMedicalObservations() throws Exception {
		var missionControlJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_MISSION_CONTROL"));
		mockMvc.perform(get("/api/v1/health/astronauts/1/readings").with(missionControlJwt))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/medical-observations/1").with(missionControlJwt))
				.andExpect(status().isForbidden());
	}

	@Test
	void healthReadingEndpointsRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/health/astronauts/1/readings"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void astronautCannotAccessAdministrationCollections() throws Exception {
		mockMvc.perform(get("/api/v1/users")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ASTRONAUT"))))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/missions")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ASTRONAUT"))))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/v1/devices")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ASTRONAUT"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void astronautCannotUseAdministrativeMutationEndpoints() throws Exception {
		var astronautJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_ASTRONAUT"));
		mockMvc.perform(put("/api/v1/users/2/activate").with(astronautJwt))
				.andExpect(status().isForbidden());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.delete("/api/v1/medical-assignments/1").with(astronautJwt))
				.andExpect(status().isForbidden());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.delete("/api/v1/missions/1").with(astronautJwt))
				.andExpect(status().isForbidden());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.put("/api/v1/devices/1/unassign").with(astronautJwt))
				.andExpect(status().isForbidden());
	}

	@Test
	void administratorCanAccessManagementCollections() throws Exception {
		var adminJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
		mockMvc.perform(get("/api/v1/users").with(adminJwt))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/missions").with(adminJwt))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/devices").with(adminJwt))
				.andExpect(status().isOk());
	}

	@Test
	void unknownApiRouteReturnsConsistentNotFoundResponse() throws Exception {
		mockMvc.perform(get("/api/v1/not-a-real-endpoint")
						.with(jwt()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("The requested resource was not found."))
				.andExpect(jsonPath("$.path").value("/api/v1/not-a-real-endpoint"));
	}

	@Test
	void nonAdministratorCannotAccessAdministrationRoutes() throws Exception {
		mockMvc.perform(post("/api/v1/auth/admin/accounts/0/activate")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ASTRONAUT"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void administratorRolePassesTheAdministrationSecurityRule() throws Exception {
		mockMvc.perform(post("/api/v1/auth/admin/accounts/0/activate")
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isBadRequest());
	}

	private String createSignedToken(Long accountId, Instant issuedAt, Instant expiresAt) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(accountId.toString())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				claims)).getTokenValue();
	}

	private UserAccount account(String email, UserRole role) {
		UserAccount account = new UserAccount();
		account.setEmail(email);
		account.setPasswordHash("test-password-hash");
		account.setRoles(EnumSet.of(role));
		account.setStatus(AccountStatus.ACTIVE);
		return account;
	}

	private Astronaut astronaut(String code) {
		Astronaut astronaut = new Astronaut();
		astronaut.setAstronautCode(code);
		astronaut.setFullName("Test Astronaut");
		astronaut.setEmail(code.toLowerCase() + "@example.com");
		astronaut.setStatus("ACTIVE");
		return astronaut;
	}
}
