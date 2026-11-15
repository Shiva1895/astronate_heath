package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.astrahealth.backend.dto.request.LoginRequest;
import com.astrahealth.backend.dto.request.RefreshTokenRequest;
import com.astrahealth.backend.dto.request.RegisterRequest;
import com.astrahealth.backend.dto.response.AuthTokenResponse;
import com.astrahealth.backend.dto.response.RegistrationResponse;
import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.RefreshToken;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidCredentialsException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.repository.RefreshTokenRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTests {

	private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
	private static final String PASSWORD = "A-Long-Unique-Password-123!";
	private static final String PASSWORD_HASH = "$2a$12$stored-hash";
	private static final String DUMMY_HASH = "$2a$12$dummy-hash";

	@Mock
	private UserAccountRepository userAccountRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private AuditService auditService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtEncoder jwtEncoder;

	private AuthenticationService authenticationService;

	@BeforeEach
	void setUp() {
		when(passwordEncoder.encode(any(String.class))).thenReturn(DUMMY_HASH);
		authenticationService = new AuthenticationService(
				userAccountRepository,
				refreshTokenRepository,
				auditService,
				passwordEncoder,
				jwtEncoder,
				new SecureRandom(),
				Clock.fixed(NOW, ZoneOffset.UTC),
				Duration.ofMinutes(15),
				Duration.ofDays(30));
	}

	@Test
	void registerCreatesPendingAstronautAccountAndStoresOnlyPasswordHash() {
		when(userAccountRepository.existsByEmail("astronaut@example.com")).thenReturn(false);
		when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
		when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
			UserAccount account = invocation.getArgument(0);
			account.setId(11L);
			return account;
		});

		RegistrationResponse response = authenticationService.register(
				registerRequest("ASTRONAUT@example.com", PASSWORD));

		assertEquals(11L, response.getId());
		assertEquals("astronaut@example.com", response.getEmail());
		assertEquals(AccountStatus.PENDING_ACTIVATION, response.getStatus());
		assertEquals(Set.of(UserRole.ASTRONAUT), response.getRoles());
		verify(auditService).record(null, "USER_CREATED", "user_account", 11L, null);

		ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
		verify(userAccountRepository).save(accountCaptor.capture());
		assertEquals(PASSWORD_HASH, accountCaptor.getValue().getPasswordHash());
		assertNotEquals(PASSWORD, accountCaptor.getValue().getPasswordHash());
	}

	@Test
	void registrationRejectsDuplicateEmail() {
		when(userAccountRepository.existsByEmail("astronaut@example.com")).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> authenticationService.register(registerRequest("astronaut@example.com", PASSWORD)));

		verify(userAccountRepository, never()).save(any(UserAccount.class));
	}

	@Test
	void registrationRejectsDuplicateUsername() {
		when(userAccountRepository.existsByEmail("astronaut@example.com")).thenReturn(false);
		when(userAccountRepository.existsByUsername("pilot")).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> authenticationService.register(registerRequest(
						"astronaut@example.com",
						"A-Long-Unique-Password-123!",
						"pilot")));

		verify(userAccountRepository, never()).save(any(UserAccount.class));
	}

	@Test
	void registrationRejectsPasswordsOverBcryptByteLimit() {
		String multibytePassword = "😀".repeat(20);

		assertThrows(
				InvalidRequestException.class,
				() -> authenticationService.register(registerRequest("astronaut@example.com", multibytePassword)));

		verify(userAccountRepository, never()).save(any(UserAccount.class));
	}

	@Test
	void activeAccountLoginIssuesJwtAndStoresOnlyRefreshTokenHash() {
		UserAccount account = createAccount(23L, AccountStatus.ACTIVE, UserRole.ASTRONAUT);
		when(userAccountRepository.findByEmail("astronaut@example.com")).thenReturn(Optional.of(account));
		when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
		when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(createJwt(account.getId()));
		when(refreshTokenRepository.save(any(RefreshToken.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		AuthTokenResponse response = authenticationService.login(
				loginRequest("astronaut@example.com", PASSWORD));

		assertEquals("signed-access-token", response.getAccessToken());
		assertEquals("Bearer", response.getTokenType());
		assertEquals(900L, response.getExpiresIn());
		ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
		verify(refreshTokenRepository).save(tokenCaptor.capture());
		assertEquals(hash(response.getRefreshToken()), tokenCaptor.getValue().getTokenHash());
		assertNotEquals(response.getRefreshToken(), tokenCaptor.getValue().getTokenHash());
		verify(auditService).record(23L, "LOGIN", "authentication", 23L, null);
	}

	@Test
	void pendingAccountCannotLogin() {
		UserAccount account = createAccount(23L, AccountStatus.PENDING_ACTIVATION, UserRole.ASTRONAUT);
		when(userAccountRepository.findByEmail("astronaut@example.com")).thenReturn(Optional.of(account));
		when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

		assertThrows(
				InvalidCredentialsException.class,
				() -> authenticationService.login(loginRequest("astronaut@example.com", PASSWORD)));

		verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
	}

	@Test
	void unknownAccountUsesGenericCredentialFailure() {
		when(userAccountRepository.findByEmail("astronaut@example.com")).thenReturn(Optional.empty());
		when(passwordEncoder.matches(PASSWORD, DUMMY_HASH)).thenReturn(false);

		assertThrows(
				InvalidCredentialsException.class,
				() -> authenticationService.login(loginRequest("astronaut@example.com", PASSWORD)));
	}

	@Test
	void incorrectPasswordForKnownAccountUsesGenericCredentialFailure() {
		UserAccount account = createAccount(23L, AccountStatus.ACTIVE, UserRole.ASTRONAUT);
		when(userAccountRepository.findByEmail("astronaut@example.com")).thenReturn(Optional.of(account));
		when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(false);

		assertThrows(
				InvalidCredentialsException.class,
				() -> authenticationService.login(loginRequest("astronaut@example.com", PASSWORD)));

		verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
	}

	@Test
	void refreshRotatesTokenAndKeepsTheSameTokenFamily() {
		UUID familyId = UUID.randomUUID();
		UserAccount account = createAccount(23L, AccountStatus.ACTIVE, UserRole.ASTRONAUT);
		RefreshToken oldToken = createRefreshToken(account, familyId, "old-refresh-token", NOW.plusSeconds(600));
		when(refreshTokenRepository.findByTokenHashForUpdate(hash("old-refresh-token")))
				.thenReturn(Optional.of(oldToken));
		when(refreshTokenRepository.save(any(RefreshToken.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(createJwt(account.getId()));

		AuthTokenResponse response = authenticationService.refresh(refreshRequest("old-refresh-token"));

		assertEquals(NOW, oldToken.getRevokedAt());
		assertNotEquals("old-refresh-token", response.getRefreshToken());
		ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
		verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(tokenCaptor.capture());
		assertEquals(familyId, tokenCaptor.getAllValues().get(1).getFamilyId());
		assertEquals(hash(response.getRefreshToken()), tokenCaptor.getAllValues().get(1).getTokenHash());
	}

	@Test
	void replayedRefreshTokenRevokesItsEntireFamily() {
		UUID familyId = UUID.randomUUID();
		UserAccount account = createAccount(23L, AccountStatus.ACTIVE, UserRole.ASTRONAUT);
		RefreshToken replayedToken = createRefreshToken(account, familyId, "old-refresh-token", NOW.plusSeconds(600));
		replayedToken.setRevokedAt(NOW.minusSeconds(1));
		RefreshToken activeToken = createRefreshToken(account, familyId, "active-refresh-token", NOW.plusSeconds(600));
		when(refreshTokenRepository.findByTokenHashForUpdate(hash("old-refresh-token")))
				.thenReturn(Optional.of(replayedToken));
		when(refreshTokenRepository.findAllByFamilyId(familyId))
				.thenReturn(List.of(replayedToken, activeToken));

		assertThrows(
				InvalidCredentialsException.class,
				() -> authenticationService.refresh(refreshRequest("old-refresh-token")));

		assertEquals(NOW, activeToken.getRevokedAt());
		verify(refreshTokenRepository).saveAll(List.of(replayedToken, activeToken));
	}

	@Test
	void logoutRevokesTheOwnedRefreshTokenFamily() {
		UUID familyId = UUID.randomUUID();
		UserAccount account = createAccount(23L, AccountStatus.ACTIVE, UserRole.ASTRONAUT);
		RefreshToken token = createRefreshToken(account, familyId, "refresh-token", NOW.plusSeconds(600));
		when(refreshTokenRepository.findByTokenHashForUpdate(hash("refresh-token")))
				.thenReturn(Optional.of(token));
		when(refreshTokenRepository.findAllByFamilyId(familyId)).thenReturn(List.of(token));

		authenticationService.logout(account.getId(), "refresh-token");

		assertEquals(NOW, token.getRevokedAt());
		verify(refreshTokenRepository).saveAll(List.of(token));
		verify(auditService).record(23L, "LOGOUT", "authentication", 23L, null);
	}

	@Test
	void refreshTokenCannotBeUsedAfterLogout() {
		UUID familyId = UUID.randomUUID();
		UserAccount account = createAccount(23L, AccountStatus.ACTIVE, UserRole.ASTRONAUT);
		RefreshToken token = createRefreshToken(account, familyId, "refresh-token", NOW.plusSeconds(600));
		when(refreshTokenRepository.findByTokenHashForUpdate(hash("refresh-token")))
				.thenReturn(Optional.of(token));
		when(refreshTokenRepository.findAllByFamilyId(familyId)).thenReturn(List.of(token));

		authenticationService.logout(account.getId(), "refresh-token");

		assertThrows(
				InvalidCredentialsException.class,
				() -> authenticationService.refresh(refreshRequest("refresh-token")));
		assertEquals(NOW, token.getRevokedAt());
		verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
	}

	@Test
	void activateAccountEnablesPendingAccount() {
		UserAccount account = createAccount(23L, AccountStatus.PENDING_ACTIVATION, UserRole.ASTRONAUT);
		when(userAccountRepository.findById(23L)).thenReturn(Optional.of(account));
		when(userAccountRepository.save(account)).thenReturn(account);

		authenticationService.activateAccount(23L);

		assertEquals(AccountStatus.ACTIVE, account.getStatus());
		verify(userAccountRepository).save(account);
	}

	@Test
	void deactivateAccountDisablesActiveAccount() {
		UserAccount account = createAccount(23L, AccountStatus.ACTIVE, UserRole.ASTRONAUT);
		RefreshToken refreshToken = createRefreshToken(
				account,
				UUID.randomUUID(),
				"refresh-token",
				NOW.plusSeconds(600));
		when(userAccountRepository.findById(23L)).thenReturn(Optional.of(account));
		when(userAccountRepository.save(account)).thenReturn(account);
		when(refreshTokenRepository.findAllByUserAccountId(23L)).thenReturn(List.of(refreshToken));

		authenticationService.deactivateAccount(23L);

		assertEquals(AccountStatus.DEACTIVATED, account.getStatus());
		assertEquals(NOW, refreshToken.getRevokedAt());
		verify(userAccountRepository).save(account);
		verify(refreshTokenRepository).saveAll(List.of(refreshToken));
	}

	@Test
	void deactivationCannotDisableTheLastActiveAdministrator() {
		UserAccount administrator = createAccount(23L, AccountStatus.ACTIVE, UserRole.ADMIN);
		when(userAccountRepository.findById(23L)).thenReturn(Optional.of(administrator));
		when(userAccountRepository.countAccountsByStatusAndRole(AccountStatus.ACTIVE, UserRole.ADMIN))
				.thenReturn(1L);

		assertThrows(InvalidRequestException.class, () -> authenticationService.deactivateAccount(23L));

		verify(userAccountRepository, never()).save(administrator);
		verify(refreshTokenRepository, never()).findAllByUserAccountId(23L);
	}

	@Test
	void bootstrapAdministratorReceivesOnlyAdministratorRole() {
		when(userAccountRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
		when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);

		authenticationService.provisionBootstrapAdministrator("ADMIN@example.com", PASSWORD);

		ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
		verify(userAccountRepository).save(accountCaptor.capture());
		assertEquals("admin@example.com", accountCaptor.getValue().getEmail());
		assertEquals(AccountStatus.ACTIVE, accountCaptor.getValue().getStatus());
		assertEquals(EnumSet.of(UserRole.ADMIN), accountCaptor.getValue().getRoles());
	}

	private RegisterRequest registerRequest(String email, String password) {
		return registerRequest(email, password, null);
	}

	private RegisterRequest registerRequest(String email, String password, String username) {
		RegisterRequest request = new RegisterRequest();
		request.setEmail(email);
		request.setPassword(password);
		request.setUsername(username);
		return request;
	}

	private LoginRequest loginRequest(String email, String password) {
		LoginRequest request = new LoginRequest();
		request.setEmail(email);
		request.setPassword(password);
		return request;
	}

	private RefreshTokenRequest refreshRequest(String tokenValue) {
		RefreshTokenRequest request = new RefreshTokenRequest();
		request.setRefreshToken(tokenValue);
		return request;
	}

	private UserAccount createAccount(Long id, AccountStatus status, UserRole role) {
		UserAccount account = new UserAccount();
		account.setId(id);
		account.setEmail("astronaut@example.com");
		account.setPasswordHash(PASSWORD_HASH);
		account.setRoles(EnumSet.of(role));
		account.setStatus(status);
		return account;
	}

	private RefreshToken createRefreshToken(
			UserAccount account,
			UUID familyId,
			String rawToken,
			Instant expiresAt) {
		RefreshToken token = new RefreshToken();
		token.setTokenHash(hash(rawToken));
		token.setFamilyId(familyId);
		token.setUserAccount(account);
		token.setExpiresAt(expiresAt);
		return token;
	}

	private Jwt createJwt(Long accountId) {
		return Jwt.withTokenValue("signed-access-token")
				.header("alg", "HS256")
				.subject(accountId.toString())
				.issuedAt(NOW)
				.expiresAt(NOW.plusSeconds(900))
				.build();
	}

	private String hash(String token) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new AssertionError(exception);
		}
	}
}
