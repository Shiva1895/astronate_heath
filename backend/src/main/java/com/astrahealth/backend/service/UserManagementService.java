package com.astrahealth.backend.service;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.astrahealth.backend.dto.request.UpdateUserProfileRequest;
import com.astrahealth.backend.dto.response.UserResponse;
import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@Service
@Transactional(readOnly = true)
public class UserManagementService {

	private final UserAccountRepository userAccountRepository;
	private final AstronautRepository astronautRepository;
	private final AuthenticationService authenticationService;

	public UserManagementService(
			UserAccountRepository userAccountRepository,
			AstronautRepository astronautRepository,
			AuthenticationService authenticationService) {
		this.userAccountRepository = userAccountRepository;
		this.astronautRepository = astronautRepository;
		this.authenticationService = authenticationService;
	}

	public Page<UserResponse> searchUsers(String query, AccountStatus status, Pageable pageable) {
		String normalizedQuery = StringUtils.hasText(query) ? query.trim() : null;
		return userAccountRepository.search(normalizedQuery, status, pageable).map(UserResponse::from);
	}

	public UserResponse getUser(Long id) {
		return UserResponse.from(getAccount(id));
	}

	public UserResponse getCurrentUser(Long id) {
		return getUser(id);
	}

	@Transactional
	public UserResponse updateProfile(Long id, UpdateUserProfileRequest request) {
		UserAccount account = getAccount(id);
		String username = normalizeUsername(request.username());
		if (username != null
				&& !username.equals(account.getUsername())
				&& userAccountRepository.existsByUsernameAndIdNot(username, id)) {
			throw new DuplicateResourceException("A user with the provided username already exists.");
		}

		account.setUsername(username);
		account.setDisplayName(request.displayName().trim());
		return UserResponse.from(userAccountRepository.save(account));
	}

	@Transactional
	public UserResponse updateRoles(Long targetUserId, Long actorUserId, Set<UserRole> requestedRoles) {
		if (targetUserId.equals(actorUserId)) {
			throw new InvalidRequestException("Users cannot change their own roles.");
		}
		if (requestedRoles == null || requestedRoles.isEmpty()) {
			throw new InvalidRequestException("At least one role is required.");
		}

		UserAccount account = getAccount(targetUserId);
		Set<UserRole> roles = EnumSet.copyOf(requestedRoles);
		boolean removesActiveAdministrator = account.getStatus() == AccountStatus.ACTIVE
				&& account.getRoles().contains(UserRole.ADMIN)
				&& !roles.contains(UserRole.ADMIN);
		if (removesActiveAdministrator
				&& userAccountRepository.countAccountsByStatusAndRole(
						AccountStatus.ACTIVE,
						UserRole.ADMIN) <= 1) {
			throw new InvalidRequestException("The last active administrator cannot be demoted.");
		}

		account.setRoles(roles);
		return UserResponse.from(userAccountRepository.save(account));
	}

	@Transactional
	public UserResponse linkAstronautProfile(Long userId, Long astronautId) {
		UserAccount account = getAccount(userId);
		if (!account.getRoles().contains(UserRole.ASTRONAUT)) {
			throw new InvalidRequestException("Only astronaut accounts can be linked to astronaut profiles.");
		}
		Astronaut astronaut = astronautRepository.findById(astronautId)
				.orElseThrow(() -> new ResourceNotFoundException("Astronaut not found with ID: " + astronautId));
		if (userAccountRepository.isAstronautProfileLinkedToAnotherUser(astronautId, userId)) {
			throw new DuplicateResourceException("Astronaut profile is already linked to another user.");
		}
		account.setAstronautProfile(astronaut);
		return UserResponse.from(userAccountRepository.save(account));
	}

	@Transactional
	public UserResponse unlinkAstronautProfile(Long userId) {
		UserAccount account = getAccount(userId);
		account.setAstronautProfile(null);
		return UserResponse.from(userAccountRepository.save(account));
	}

	@Transactional
	public void activateAccount(Long id) {
		authenticationService.activateAccount(id);
	}

	@Transactional
	public void deactivateAccount(Long id) {
		authenticationService.deactivateAccount(id);
	}

	private UserAccount getAccount(Long id) {
		return userAccountRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User account not found with ID: " + id));
	}

	private String normalizeUsername(String username) {
		return StringUtils.hasText(username) ? username.trim().toLowerCase(Locale.ROOT) : null;
	}
}
