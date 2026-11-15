package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.AlertHistory;
import com.astrahealth.backend.entity.AlertHistoryAction;
import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AlertStatus;
import com.astrahealth.backend.entity.AnomalyRule;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.dto.request.CreateAlertRequest;
import com.astrahealth.backend.repository.AlertHistoryRepository;
import com.astrahealth.backend.repository.AlertRepository;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;
import com.astrahealth.backend.security.HealthAccessService;

@ExtendWith(MockitoExtension.class)
class AlertServiceTests {

	@Mock
	private AlertRepository alertRepository;
	@Mock
	private AlertHistoryRepository historyRepository;
	@Mock
	private AstronautRepository astronautRepository;
	@Mock
	private UserAccountRepository userAccountRepository;
	@Mock
	private MedicalAssignmentRepository assignmentRepository;
	@Mock
	private HealthAccessService healthAccessService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private Clock clock;

	@InjectMocks
	private AlertService alertService;

	@BeforeEach
	void setUp() {
		lenient().when(clock.instant()).thenReturn(Instant.parse("2026-11-15T12:00:00Z"));
	}

	@Test
	void assignedMedicalOfficerCanManageCompleteAlertWorkflowAndHistoryIsRecorded() {
		Alert alert = alert();
		UserAccount admin = account(4L, UserRole.ADMIN);
		when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
		when(userAccountRepository.findById(4L)).thenReturn(Optional.of(admin));
		when(alertRepository.save(alert)).thenReturn(alert);
		when(historyRepository.save(any(AlertHistory.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		alertService.acknowledge(1L, 4L);
		alertService.escalate(1L, 4L);
		alertService.resolve(1L, 4L);
		alertService.close(1L, 4L);

		assertEquals(AlertStatus.CLOSED, alert.getStatus());
		assertEquals(4L, alert.getLastHandledBy().getId());
		verify(historyRepository, org.mockito.Mockito.times(4)).save(any(AlertHistory.class));
	}

	@Test
	void cannotCloseAnAlertBeforeItIsResolved() {
		Alert alert = alert();
		when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
		when(userAccountRepository.findById(4L)).thenReturn(Optional.of(account(4L, UserRole.ADMIN)));

		assertThrows(
				com.astrahealth.backend.exception.InvalidRequestException.class,
				() -> alertService.close(1L, 4L));

		verify(alertRepository, never()).save(alert);
	}

	@Test
	void unassignedMedicalOfficerCannotAcknowledgeAlert() {
		Alert alert = alert();
		when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
		when(userAccountRepository.findById(8L)).thenReturn(Optional.of(account(8L, UserRole.MEDICAL_OFFICER)));
		when(assignmentRepository.existsByMedicalOfficerIdAndAstronautId(8L, 7L)).thenReturn(false);

		assertThrows(AccessDeniedException.class, () -> alertService.acknowledge(1L, 8L));

		verify(alertRepository, never()).save(alert);
	}

	@Test
	void authorizedAdministratorCanCreateManualAlert() {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		UserAccount admin = account(4L, UserRole.ADMIN);
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(userAccountRepository.findById(4L)).thenReturn(Optional.of(admin));
		when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(historyRepository.save(any(AlertHistory.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var response = alertService.createManual(
				new CreateAlertRequest(7L, "Review requested", "Operational note",
						"Manual authorized alert.", AlertPriority.MEDIUM),
				4L);

		assertEquals(AlertStatus.OPEN, response.status());
		verify(historyRepository).save(any(AlertHistory.class));
	}

	@Test
	void duplicateRuleEventDoesNotCreateAnotherAlert() {
		HealthReading reading = new HealthReading();
		reading.setReadingType(ReadingType.HEART_RATE);
		AnomalyRule rule = new AnomalyRule();
		when(alertRepository.existsBySourceReadingIdAndAnomalyRuleId(null, null)).thenReturn(true);

		alertService.createRuleAlert(reading, rule, "Configured threshold was crossed.");

		verify(alertRepository, never()).save(any(Alert.class));
		verify(historyRepository, never()).save(any(AlertHistory.class));
	}

	@Test
	void ruleAlertCreationRecordsItsSourceAndHistoryAction() {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		HealthReading reading = new HealthReading();
		reading.setAstronaut(astronaut);
		reading.setReadingType(ReadingType.HEART_RATE);
		reading.setValue(new java.math.BigDecimal("150"));
		reading.setUnit("bpm");
		AnomalyRule rule = new AnomalyRule();
		rule.setDescription("Configured test rule");
		rule.setPriority(AlertPriority.HIGH);
		when(alertRepository.existsBySourceReadingIdAndAnomalyRuleId(null, null)).thenReturn(false);
		when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(historyRepository.save(any(AlertHistory.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		alertService.createRuleAlert(reading, rule, "Value crossed configured limit.");

		org.mockito.ArgumentCaptor<Alert> alertCaptor = org.mockito.ArgumentCaptor.forClass(Alert.class);
		verify(alertRepository).save(alertCaptor.capture());
		assertEquals(reading, alertCaptor.getValue().getSourceReading());
		assertEquals(rule, alertCaptor.getValue().getAnomalyRule());
		org.mockito.ArgumentCaptor<AlertHistory> historyCaptor =
				org.mockito.ArgumentCaptor.forClass(AlertHistory.class);
		verify(historyRepository).save(historyCaptor.capture());
		assertEquals(AlertHistoryAction.RULE_TRIGGERED, historyCaptor.getValue().getAction());
	}

	private Alert alert() {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		Alert alert = new Alert();
		alert.setAstronaut(astronaut);
		alert.setStatus(AlertStatus.OPEN);
		return alert;
	}

	private UserAccount account(Long id, UserRole role) {
		UserAccount account = new UserAccount();
		account.setId(id);
		account.setRoles(EnumSet.of(role));
		return account;
	}
}
