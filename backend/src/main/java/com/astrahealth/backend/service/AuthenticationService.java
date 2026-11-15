package com.astrahealth.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.astrahealth.backend.dto.request.LoginRequest;
import com.astrahealth.backend.dto.request.RegisterRequest;
import com.astrahealth.backend.dto.request.RefreshTokenRequest;
import com.astrahealth.backend.dto.response.AuthTokenResponse;
import com.astrahealth.backend.dto.response.RegistrationResponse;
import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.RefreshToken;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidCredentialsException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.RefreshTokenRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@Service
public class AuthenticationService {

	private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

	private final UserAccountRepository userAccountRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final AuditService auditService;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final SecureRandom secureRandom;
	private final Clock clock;
	private final Duration accessTokenTtl;
	private final Duration refreshTokenTtl;
	private final String dummyPasswordHash;

	public AuthenticationService(
			UserAccountRepository userAccountRepository,
			RefreshTokenRepository refreshTokenRepository,
			AuditService auditService,
			PasswordEncoder passwordEncoder,
			JwtEncoder jwtEncoder,
			SecureRandom secureRandom,
			Clock clock,
			@Value("${astra.security.jwt.access-token-ttl:15m}") Duration accessTokenTtl,
			@Value("${astra.security.jwt.refresh-token-ttl:30d}") Duration refreshTokenTtl) {
		this.userAccountRepository = userAccountRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.auditService = auditService;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.secureRandom = secureRandom;
		this.clock = clock;
		this.accessTokenTtl = accessTokenTtl;
		this.refreshTokenTtl = refreshTokenTtl;
		if (accessTokenTtl.isZero() || accessTokenTtl.isNegative()
				|| refreshTokenTtl.isZero() || refreshTokenTtl.isNegative()) {
			throw new IllegalArgumentException("JWT access and refresh token lifetimes must be positive.");
		}
		this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
	}

	@Transactional
	public RegistrationResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.getEmail());
		validatePasswordLength(request.getPassword());
		if (userAccountRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("An account with the provided email already exists.");
		}

		String username = normalizeUsername(request.getUsername());
		if (username != null && userAccountRepository.existsByUsername(username)) {
			throw new DuplicateResourceException("A user with the provided username already exists.");
		}

		UserAccount account = new UserAccount();
		account.setEmail(email);
		account.setUsername(username);
		account.setDisplayName(StringUtils.hasText(request.getDisplayName())
				? request.getDisplayName().trim()
				: null);
		account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		account.setRoles(EnumSet.of(UserRole.ASTRONAUT));
		account.setStatus(AccountStatus.PENDING_ACTIVATION);
		UserAccount savedAccount = userAccountRepository.save(account);
		auditService.record(null, "USER_CREATED", "user_account", savedAccount.getId(), null);
		return toRegistrationResponse(savedAccount);
	}

	@Transactional
	public AuthTokenResponse login(LoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
			throw new InvalidCredentialsException();
		}

		UserAccount account = userAccountRepository.findByEmail(email).orElse(null);
		String passwordHash = account == null ? dummyPasswordHash : account.getPasswordHash();
		boolean passwordMatches = passwordEncoder.matches(request.getPassword(), passwordHash);
		if (account == null || account.getStatus() != AccountStatus.ACTIVE || !passwordMatches) {
			throw new InvalidCredentialsException();
		}

		AuthTokenResponse tokens = issueTokens(account, UUID.randomUUID());
		auditService.record(account.getId(), "LOGIN", "authentication", account.getId(), null);
		return tokens;
	}

	@Transactional(noRollbackFor = InvalidCredentialsException.class)
	public AuthTokenResponse refresh(RefreshTokenRequest request) {
		Instant now = clock.instant();
		RefreshToken current = refreshTokenRepository
				.findByTokenHashForUpdate(hashToken(request.getRefreshToken()))
				.orElseThrow(InvalidCredentialsException::new);

		if (current.getRevokedAt() != null
				|| !current.getExpiresAt().isAfter(now)
				|| current.getUserAccount().getStatus() != AccountStatus.ACTIVE) {
			revokeFamily(current.getFamilyId(), now);
			throw new InvalidCredentialsException();
		}

		current.setRevokedAt(now);
		refreshTokenRepository.save(current);
		return issueTokens(current.getUserAccount(), current.getFamilyId());
	}

	@Transactional
	public void logout(Long authenticatedAccountId, String refreshTokenValue) {
		if (StringUtils.hasText(refreshTokenValue)) {
			refreshTokenRepository.findByTokenHashForUpdate(hashToken(refreshTokenValue))
					.filter(token -> token.getUserAccount().getId().equals(authenticatedAccountId))
					.ifPresent(token -> revokeFamily(token.getFamilyId(), clock.instant()));
		}
		auditService.record(authenticatedAccountId, "LOGOUT", "authentication", authenticatedAccountId, null);
	}

	@Transactional
	public void activateAccount(Long accountId) {
		UserAccount account = getAccount(accountId);
		account.setStatus(AccountStatus.ACTIVE);
		userAccountRepository.save(account);
	}

	@Transactional
	public void deactivateAccount(Long accountId) {
		UserAccount account = getAccount(accountId);
		if (account.getRoles().contains(UserRole.ADMIN)
				&& userAccountRepository.countAccountsByStatusAndRole(AccountStatus.ACTIVE, UserRole.ADMIN) <= 1) {
			throw new InvalidRequestException("The last active administrator cannot be deactivated.");
		}
		revokeAccountRefreshTokens(accountId, clock.instant());
		account.setStatus(AccountStatus.DEACTIVATED);
		userAccountRepository.save(account);
	}

	@Transactional
	public void provisionBootstrapAdministrator(String emailValue, String rawPassword) {
		String email = normalizeEmail(emailValue);
		validatePasswordLength(rawPassword);

		UserAccount existingAccount = userAccountRepository.findByEmail(email).orElse(null);
		if (existingAccount != null) {
			if (existingAccount.getStatus() == AccountStatus.ACTIVE
					&& existingAccount.getRoles().contains(UserRole.ADMIN)) {
				return;
			}
			throw new IllegalStateException(
					"Bootstrap administrator email is already assigned to a non-active administrator account.");
		}

		UserAccount administrator = new UserAccount();
		administrator.setEmail(email);
		administrator.setPasswordHash(passwordEncoder.encode(rawPassword));
		administrator.setRoles(EnumSet.of(UserRole.ADMIN));
		administrator.setStatus(AccountStatus.ACTIVE);
		userAccountRepository.save(administrator);
	}

	private AuthTokenResponse issueTokens(UserAccount account, UUID familyId) {
		Instant now = clock.instant();
		Instant expiresAt = now.plus(accessTokenTtl);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("astra-health")
				.subject(account.getId().toString())
				.issuedAt(now)
				.expiresAt(expiresAt)
				.id(UUID.randomUUID().toString())
				.claim("email", account.getEmail())
				.claim("roles", account.getRoles().stream().map(Enum::name).sorted().toList())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

		String refreshTokenValue = generateRefreshTokenValue();
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setTokenHash(hashToken(refreshTokenValue));
		refreshToken.setFamilyId(familyId);
		refreshToken.setUserAccount(account);
		refreshToken.setExpiresAt(now.plus(refreshTokenTtl));
		refreshTokenRepository.save(refreshToken);

		return new AuthTokenResponse(accessToken, accessTokenTtl.toSeconds(), refreshTokenValue);
	}

	private String generateRefreshTokenValue() {
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}

	private String hashToken(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is required to validate refresh tokens.", exception);
		}
	}

	private void revokeFamily(UUID familyId, Instant revokedAt) {
		List<RefreshToken> familyTokens = refreshTokenRepository.findAllByFamilyId(familyId);
		familyTokens.stream()
				.filter(token -> token.getRevokedAt() == null)
				.forEach(token -> token.setRevokedAt(revokedAt));
		refreshTokenRepository.saveAll(familyTokens);
	}

	private void revokeAccountRefreshTokens(Long accountId, Instant revokedAt) {
		List<RefreshToken> accountTokens = refreshTokenRepository.findAllByUserAccountId(accountId);
		accountTokens.stream()
				.filter(token -> token.getRevokedAt() == null)
				.forEach(token -> token.setRevokedAt(revokedAt));
		refreshTokenRepository.saveAll(accountTokens);
	}

	private UserAccount getAccount(Long accountId) {
		return userAccountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceNotFoundException("User account not found with ID: " + accountId));
	}

	private RegistrationResponse toRegistrationResponse(UserAccount account) {
		return new RegistrationResponse(account.getId(), account.getEmail(), account.getStatus(), account.getRoles());
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeUsername(String username) {
		return StringUtils.hasText(username) ? username.trim().toLowerCase(Locale.ROOT) : null;
	}

	private void validatePasswordLength(String password) {
		if (password.codePointCount(0, password.length()) < 12
				|| password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
			throw new InvalidRequestException(
					"Password must contain at least 12 characters and not exceed 72 UTF-8 bytes.");
		}
	}
}
