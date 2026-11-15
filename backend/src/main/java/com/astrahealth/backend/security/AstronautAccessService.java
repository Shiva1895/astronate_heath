package com.astrahealth.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@Component("astronautAccessService")
public class AstronautAccessService {

	private final UserAccountRepository userAccountRepository;
	private final MedicalAssignmentRepository assignmentRepository;

	public AstronautAccessService(
			UserAccountRepository userAccountRepository,
			MedicalAssignmentRepository assignmentRepository) {
		this.userAccountRepository = userAccountRepository;
		this.assignmentRepository = assignmentRepository;
	}

	public boolean canView(long astronautId, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		if (authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
			return true;
		}

		Long accountId;
		try {
			accountId = Long.valueOf(authentication.getName());
		} catch (NumberFormatException exception) {
			return false;
		}

		UserAccount account = userAccountRepository.findById(accountId).orElse(null);
		if (account == null) {
			return false;
		}
		if (account.getRoles().contains(UserRole.ASTRONAUT)
				&& account.getAstronautProfile() != null
				&& account.getAstronautProfile().getId().equals(astronautId)) {
			return true;
		}
		return account.getRoles().contains(UserRole.MEDICAL_OFFICER)
				&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(accountId, astronautId);
	}
}
