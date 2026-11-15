package com.astrahealth.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationConverterTests {

	private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

	@Mock
	private UserAccountRepository userAccountRepository;

	private JwtAuthenticationConverter converter;

	@BeforeEach
	void setUp() {
		converter = new SecurityConfiguration().jwtAuthenticationConverter(userAccountRepository);
	}

	@Test
	void activeAccountRolesAreLoadedFromTheDatabase() {
		UserAccount account = createAccount(AccountStatus.ACTIVE, UserRole.MEDICAL_OFFICER);
		when(userAccountRepository.findById(9L)).thenReturn(Optional.of(account));

		var authentication = converter.convert(createJwt());

		assertEquals("9", authentication.getName());
		assertTrue(authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_MEDICAL_OFFICER")));
		assertFalse(authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
	}

	@Test
	void deactivatedAccountCannotUseAnExistingAccessToken() {
		when(userAccountRepository.findById(9L))
				.thenReturn(Optional.of(createAccount(AccountStatus.DEACTIVATED, UserRole.ASTRONAUT)));

		assertThrows(InvalidBearerTokenException.class, () -> converter.convert(createJwt()));
	}

	private UserAccount createAccount(AccountStatus status, UserRole role) {
		UserAccount account = new UserAccount();
		account.setId(9L);
		account.setEmail("user@example.com");
		account.setRoles(EnumSet.of(role));
		account.setStatus(status);
		return account;
	}

	private Jwt createJwt() {
		return Jwt.withTokenValue("signed-token")
				.header("alg", "HS256")
				.subject("9")
				.claim("roles", java.util.List.of("ADMIN"))
				.issuedAt(NOW)
				.expiresAt(NOW.plusSeconds(900))
				.build();
	}
}
