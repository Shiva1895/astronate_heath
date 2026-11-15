package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.astrahealth.backend.dto.request.UpdateUserProfileRequest;
import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTests {

	@Mock
	private UserAccountRepository userAccountRepository;

	@Mock
	private AstronautRepository astronautRepository;

	@Mock
	private AuthenticationService authenticationService;

	@InjectMocks
	private UserManagementService userManagementService;

	@Test
	void profileUpdateRejectsDuplicateUsername() {
		UserAccount account = account(2L, UserRole.ASTRONAUT);
		when(userAccountRepository.findById(2L)).thenReturn(Optional.of(account));
		when(userAccountRepository.existsByUsernameAndIdNot("pilot", 2L)).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> userManagementService.updateProfile(2L, new UpdateUserProfileRequest("pilot", "Pilot")));
		verify(userAccountRepository, never()).save(any(UserAccount.class));
	}

	@Test
	void userCannotChangeOwnRoles() {
		assertThrows(
				InvalidRequestException.class,
				() -> userManagementService.updateRoles(2L, 2L, Set.of(UserRole.ADMIN)));
		verify(userAccountRepository, never()).findById(2L);
	}

	@Test
	void lastActiveAdministratorCannotBeDemoted() {
		UserAccount administrator = account(3L, UserRole.ADMIN);
		when(userAccountRepository.findById(3L)).thenReturn(Optional.of(administrator));
		when(userAccountRepository.countAccountsByStatusAndRole(AccountStatus.ACTIVE, UserRole.ADMIN))
				.thenReturn(1L);

		assertThrows(
				InvalidRequestException.class,
				() -> userManagementService.updateRoles(3L, 2L, Set.of(UserRole.ASTRONAUT)));
		verify(userAccountRepository, never()).save(administrator);
	}

	@Test
	void astronautProfileCanBeLinkedWithoutCascade() {
		UserAccount account = account(2L, UserRole.ASTRONAUT);
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		astronaut.setAstronautCode("AST-007");
		when(userAccountRepository.findById(2L)).thenReturn(Optional.of(account));
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(userAccountRepository.isAstronautProfileLinkedToAnotherUser(7L, 2L)).thenReturn(false);
		when(userAccountRepository.save(account)).thenReturn(account);

		userManagementService.linkAstronautProfile(2L, 7L);

		verify(userAccountRepository).save(account);
	}

	@Test
	void astronautProfileCannotBeLinkedToTwoUsers() {
		UserAccount account = account(2L, UserRole.ASTRONAUT);
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		when(userAccountRepository.findById(2L)).thenReturn(Optional.of(account));
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(userAccountRepository.isAstronautProfileLinkedToAnotherUser(7L, 2L)).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> userManagementService.linkAstronautProfile(2L, 7L));
		verify(userAccountRepository, never()).save(account);
	}

	private UserAccount account(Long id, UserRole role) {
		UserAccount account = new UserAccount();
		account.setId(id);
		account.setEmail("user" + id + "@example.com");
		account.setUsername("user" + id);
		account.setRoles(EnumSet.of(role));
		account.setStatus(AccountStatus.ACTIVE);
		return account;
	}
}
