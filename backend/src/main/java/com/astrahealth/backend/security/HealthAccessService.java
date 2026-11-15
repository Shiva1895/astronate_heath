package com.astrahealth.backend.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@Component("healthAccessService")
public class HealthAccessService {

	private final UserAccountRepository userAccountRepository;
	private final MedicalAssignmentRepository medicalAssignmentRepository;

	public HealthAccessService(
			UserAccountRepository userAccountRepository,
			MedicalAssignmentRepository medicalAssignmentRepository) {
		this.userAccountRepository = userAccountRepository;
		this.medicalAssignmentRepository = medicalAssignmentRepository;
	}

	public boolean canAccessAstronaut(Long astronautId, Authentication authentication) {
		if (astronautId == null || authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		long userId;
		try {
			userId = Long.parseLong(authentication.getName());
		} catch (NumberFormatException exception) {
			return false;
		}
		UserAccount account = userAccountRepository.findById(userId).orElse(null);
		if (account == null) {
			return false;
		}
		if (account.getRoles().contains(UserRole.ADMIN)) {
			return true;
		}
		if (account.getRoles().contains(UserRole.ASTRONAUT)
				&& account.getAstronautProfile() != null
				&& astronautId.equals(account.getAstronautProfile().getId())) {
			return true;
		}
		return account.getRoles().contains(UserRole.MEDICAL_OFFICER)
				&& medicalAssignmentRepository.existsByMedicalOfficerIdAndAstronautId(userId, astronautId);
	}

	public void requireAccess(Long astronautId, Long userId) {
		UserAccount account = userAccountRepository.findById(userId).orElse(null);
		if (account == null) {
			throw new AccessDeniedException("The authenticated account cannot access health data.");
		}
		if (account.getRoles().contains(UserRole.ADMIN)) {
			return;
		}
		if (account.getRoles().contains(UserRole.ASTRONAUT)
				&& account.getAstronautProfile() != null
				&& astronautId.equals(account.getAstronautProfile().getId())) {
			return;
		}
		if (account.getRoles().contains(UserRole.MEDICAL_OFFICER)
				&& medicalAssignmentRepository.existsByMedicalOfficerIdAndAstronautId(userId, astronautId)) {
			return;
		}
		throw new AccessDeniedException("The authenticated account cannot access health data.");
	}
}
