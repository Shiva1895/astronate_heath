package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class MedicalAssignmentServiceTests {

	@Mock
	private MedicalAssignmentRepository assignmentRepository;

	@Mock
	private UserAccountRepository userAccountRepository;

	@Mock
	private AstronautRepository astronautRepository;

	@InjectMocks
	private MedicalAssignmentService assignmentService;

	@Test
	void assignmentRequiresMedicalOfficerRole() {
		UserAccount user = new UserAccount();
		user.setId(2L);
		user.setStatus(AccountStatus.ACTIVE);
		user.setRoles(EnumSet.of(UserRole.ASTRONAUT));
		when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user));

		assertThrows(InvalidRequestException.class, () -> assignmentService.assign(2L, 7L));
		verify(assignmentRepository, never()).save(any());
	}

	@Test
	void duplicateMedicalAssignmentIsRejected() {
		UserAccount officer = new UserAccount();
		officer.setId(2L);
		officer.setRoles(EnumSet.of(UserRole.MEDICAL_OFFICER));
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		when(userAccountRepository.findById(2L)).thenReturn(Optional.of(officer));
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(assignmentRepository.existsByMedicalOfficerIdAndAstronautId(2L, 7L)).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> assignmentService.assign(2L, 7L));
		verify(assignmentRepository, never()).save(any());
	}

	@Test
	void medicalOfficerCanRetrieveAssignedAstronauts() {
		UserAccount officer = new UserAccount();
		officer.setId(2L);
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		astronaut.setAstronautCode("AST-007");
		astronaut.setFullName("Test Astronaut");
		astronaut.setStatus("ACTIVE");
		com.astrahealth.backend.entity.MedicalAssignment assignment =
				new com.astrahealth.backend.entity.MedicalAssignment();
		assignment.setMedicalOfficer(officer);
		assignment.setAstronaut(astronaut);
		when(assignmentRepository.findAllByMedicalOfficerId(
				org.mockito.ArgumentMatchers.eq(2L),
				org.mockito.ArgumentMatchers.any()))
				.thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(assignment)));

		var page = assignmentService.getAssignedAstronauts(
				2L,
				org.springframework.data.domain.PageRequest.of(0, 20));

		assertEquals(7L, page.getContent().get(0).getId());
		assertEquals("AST-007", page.getContent().get(0).getAstronautCode());
	}
}
