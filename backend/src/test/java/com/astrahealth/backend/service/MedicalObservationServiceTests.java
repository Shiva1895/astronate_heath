package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.astrahealth.backend.dto.request.MedicalObservationRequest;
import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.FollowUpStatus;
import com.astrahealth.backend.entity.MedicalAssignment;
import com.astrahealth.backend.entity.MedicalFollowUpAction;
import com.astrahealth.backend.entity.MedicalObservation;
import com.astrahealth.backend.entity.MedicalObservationHistory;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.repository.AlertRepository;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.MedicalFollowUpActionRepository;
import com.astrahealth.backend.repository.MedicalObservationHistoryRepository;
import com.astrahealth.backend.repository.MedicalObservationRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class MedicalObservationServiceTests {

	@Mock
	private MedicalObservationRepository observationRepository;
	@Mock
	private MedicalObservationHistoryRepository historyRepository;
	@Mock
	private MedicalFollowUpActionRepository followUpRepository;
	@Mock
	private AstronautRepository astronautRepository;
	@Mock
	private AlertRepository alertRepository;
	@Mock
	private HealthReadingRepository readingRepository;
	@Mock
	private UserAccountRepository userAccountRepository;
	@Mock
	private MedicalAssignmentRepository assignmentRepository;
	@Mock
	private NotificationService notificationService;
	@InjectMocks
	private MedicalObservationService observationService;

	@Test
	void assignedMedicalOfficerCanCreateObservationAndVersionIsAudited() {
		Astronaut astronaut = astronaut(7L);
		UserAccount officer = account(8L, UserRole.MEDICAL_OFFICER);
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(userAccountRepository.findById(8L)).thenReturn(Optional.of(officer));
		when(assignmentRepository.existsByMedicalOfficerIdAndAstronautId(8L, 7L)).thenReturn(true);
		when(observationRepository.saveAndFlush(any(MedicalObservation.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(historyRepository.save(any(MedicalObservationHistory.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var response = observationService.create(
				new MedicalObservationRequest(7L, "Confidential clinical note.", null, null, null),
				8L);

		assertEquals(7L, response.astronautId());
		assertEquals(8L, response.medicalOfficerUserId());
		assertEquals("Confidential clinical note.", response.observationText());
		verify(historyRepository).save(any(MedicalObservationHistory.class));
	}

	@Test
	void unassignedMedicalOfficerCannotReadPrivateObservation() {
		MedicalObservation observation = new MedicalObservation();
		observation.setAstronaut(astronaut(7L));
		when(observationRepository.findById(2L)).thenReturn(Optional.of(observation));
		when(userAccountRepository.findById(8L)).thenReturn(
				Optional.of(account(8L, UserRole.MEDICAL_OFFICER)));
		when(assignmentRepository.existsByMedicalOfficerIdAndAstronautId(8L, 7L)).thenReturn(false);

		assertThrows(AccessDeniedException.class, () -> observationService.get(2L, 8L));
	}

	@Test
	void observationCannotReferenceAnotherAstronautsAlert() {
		Astronaut subject = astronaut(7L);
		Alert alert = new Alert();
		alert.setAstronaut(astronaut(9L));
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(subject));
		when(userAccountRepository.findById(4L)).thenReturn(Optional.of(account(4L, UserRole.ADMIN)));
		when(alertRepository.findById(12L)).thenReturn(Optional.of(alert));

		assertThrows(
				InvalidRequestException.class,
				() -> observationService.create(
						new MedicalObservationRequest(7L, "Private note.", 12L, null, null),
						4L));

		verify(observationRepository, never()).saveAndFlush(any(MedicalObservation.class));
	}

	@Test
	void assignedMedicalOfficerCanCompleteFollowUpAndHistoryIsRecorded() {
		Astronaut astronaut = astronaut(7L);
		UserAccount officer = account(8L, UserRole.MEDICAL_OFFICER);
		MedicalObservation observation = new MedicalObservation();
		observation.setAstronaut(astronaut);
		MedicalFollowUpAction action = new MedicalFollowUpAction();
		action.setObservation(observation);
		action.setStatus(FollowUpStatus.PENDING);
		action.setCreatedBy(officer);
		action.setUpdatedBy(officer);
		when(observationRepository.findById(2L)).thenReturn(Optional.of(observation));
		when(userAccountRepository.findById(8L)).thenReturn(Optional.of(officer));
		when(assignmentRepository.existsByMedicalOfficerIdAndAstronautId(8L, 7L)).thenReturn(true);
		when(followUpRepository.findByIdAndObservationId(3L, 2L)).thenReturn(Optional.of(action));
		when(followUpRepository.save(action)).thenReturn(action);
		when(historyRepository.save(any(MedicalObservationHistory.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var response = observationService.updateFollowUpStatus(2L, 3L, FollowUpStatus.COMPLETED, 8L);

		assertEquals(FollowUpStatus.COMPLETED, response.status());
		verify(historyRepository).save(any(MedicalObservationHistory.class));
	}

	@Test
	void administratorFollowUpInboxFiltersAndPaginatesInTheDatabase() {
		UserAccount administrator = account(1L, UserRole.ADMIN);
		PageRequest pageable = PageRequest.of(0, 20);
		when(userAccountRepository.findById(1L)).thenReturn(Optional.of(administrator));
		when(followUpRepository.findAllByStatusOrderByDueDateAsc(FollowUpStatus.PENDING, pageable))
				.thenReturn(Page.empty(pageable));

		var response = observationService.followUpsForAssignedAstronauts(1L, pageable);

		assertEquals(0, response.getTotalElements());
		verify(followUpRepository).findAllByStatusOrderByDueDateAsc(FollowUpStatus.PENDING, pageable);
	}

	@Test
	void medicalOfficerFollowUpInboxUsesOnlyAssignedAstronautsAndPendingTasks() {
		UserAccount officer = account(8L, UserRole.MEDICAL_OFFICER);
		MedicalAssignment assignment = new MedicalAssignment();
		assignment.setAstronaut(astronaut(7L));
		PageRequest pageable = PageRequest.of(0, 20);
		when(userAccountRepository.findById(8L)).thenReturn(Optional.of(officer));
		when(assignmentRepository.findAllByMedicalOfficerId(8L)).thenReturn(List.of(assignment));
		when(followUpRepository.findAssignedPendingByAstronautIds(
				List.of(7L), FollowUpStatus.PENDING, 8L, pageable))
				.thenReturn(Page.empty(pageable));

		var response = observationService.followUpsForAssignedAstronauts(8L, pageable);

		assertEquals(0, response.getTotalElements());
		verify(followUpRepository).findAssignedPendingByAstronautIds(
				List.of(7L), FollowUpStatus.PENDING, 8L, pageable);
	}

	private Astronaut astronaut(Long id) {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(id);
		astronaut.setAstronautCode("AST-" + id);
		astronaut.setFullName("Test Astronaut");
		astronaut.setStatus("ACTIVE");
		return astronaut;
	}

	private UserAccount account(Long id, UserRole role) {
		UserAccount account = new UserAccount();
		account.setId(id);
		account.setRoles(EnumSet.of(role));
		account.setStatus(AccountStatus.ACTIVE);
		return account;
	}
}
