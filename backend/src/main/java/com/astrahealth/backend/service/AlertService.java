package com.astrahealth.backend.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.request.CreateAlertRequest;
import com.astrahealth.backend.dto.response.AlertHistoryResponse;
import com.astrahealth.backend.dto.response.AlertResponse;
import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.AlertHistory;
import com.astrahealth.backend.entity.AlertHistoryAction;
import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AlertStatus;
import com.astrahealth.backend.entity.AnomalyRule;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.NotificationType;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AlertHistoryRepository;
import com.astrahealth.backend.repository.AlertRepository;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;
import com.astrahealth.backend.security.HealthAccessService;

@Service
@Transactional(readOnly = true)
public class AlertService {

	private static final Set<AlertStatus> ACTIVE_STATUSES =
			Set.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED, AlertStatus.ESCALATED);

	private final AlertRepository alertRepository;
	private final AlertHistoryRepository historyRepository;
	private final AstronautRepository astronautRepository;
	private final UserAccountRepository userAccountRepository;
	private final MedicalAssignmentRepository assignmentRepository;
	private final HealthAccessService healthAccessService;
	private final NotificationService notificationService;
	private final Clock clock;

	public AlertService(
			AlertRepository alertRepository,
			AlertHistoryRepository historyRepository,
			AstronautRepository astronautRepository,
			UserAccountRepository userAccountRepository,
			MedicalAssignmentRepository assignmentRepository,
			HealthAccessService healthAccessService,
			NotificationService notificationService,
			Clock clock) {
		this.alertRepository = alertRepository;
		this.historyRepository = historyRepository;
		this.astronautRepository = astronautRepository;
		this.userAccountRepository = userAccountRepository;
		this.assignmentRepository = assignmentRepository;
		this.healthAccessService = healthAccessService;
		this.notificationService = notificationService;
		this.clock = clock;
	}

	@Transactional
	public AlertResponse createManual(CreateAlertRequest request, Long userId) {
		Astronaut astronaut = astronautRepository.findById(request.astronautId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Astronaut not found with ID: " + request.astronautId()));
		UserAccount actor = requireMedicalManager(userId, astronaut.getId());
		Alert alert = new Alert();
		alert.setAstronaut(astronaut);
		alert.setTitle(request.title().trim());
		alert.setDescription(request.description().trim());
		alert.setReason(request.reason().trim());
		alert.setPriority(request.priority());
		alert.setCreatedBy(actor);
		alert = alertRepository.save(alert);
		record(alert, actor, AlertHistoryAction.CREATED, "Alert created by an authorized user.");
		notificationService.notifyMedicalOfficers(
				astronaut.getId(),
				NotificationType.ALERT,
				"New alert",
				"An alert for an assigned astronaut requires review.",
				"alert",
				alert.getId());
		return AlertResponse.from(alert);
	}

	@Transactional
	public void createRuleAlert(HealthReading reading, AnomalyRule rule, String reason) {
		if (alertRepository.existsBySourceReadingIdAndAnomalyRuleId(reading.getId(), rule.getId())) {
			return;
		}
		Alert alert = new Alert();
		alert.setAstronaut(reading.getAstronaut());
		alert.setSourceReading(reading);
		alert.setAnomalyRule(rule);
		alert.setTitle("Configured " + reading.getReadingType() + " rule triggered");
		alert.setDescription(rule.getDescription());
		alert.setReason(reason);
		alert.setPriority(rule.getPriority());
		alert = alertRepository.save(alert);
		record(alert, null, AlertHistoryAction.RULE_TRIGGERED, "Configured rule triggered by a health reading.");
		notificationService.notifyMedicalOfficers(
				reading.getAstronaut().getId(),
				NotificationType.ALERT,
				"New alert",
				"An alert for an assigned astronaut requires review.",
				"alert",
				alert.getId());
	}

	public Page<AlertResponse> listAll(
			AlertStatus status,
			AlertPriority priority,
			Pageable pageable) {
		return alertRepository.search(null, status, priority, pageable).map(AlertResponse::from);
	}

	public Page<AlertResponse> listForAstronaut(
			Long astronautId,
			AlertStatus status,
			AlertPriority priority,
			Long userId,
			Pageable pageable) {
		requireAstronautExists(astronautId);
		healthAccessService.requireAccess(astronautId, userId);
		return alertRepository.search(astronautId, status, priority, pageable).map(AlertResponse::from);
	}

	public AlertResponse get(Long id, Long userId) {
		Alert alert = getAlert(id);
		healthAccessService.requireAccess(alert.getAstronaut().getId(), userId);
		return AlertResponse.from(alert);
	}

	public Page<AlertHistoryResponse> history(Long id, Long userId, Pageable pageable) {
		Alert alert = getAlert(id);
		healthAccessService.requireAccess(alert.getAstronaut().getId(), userId);
		return historyRepository.findAllByAlertIdOrderByOccurredAtDesc(id, pageable)
				.map(AlertHistoryResponse::from);
	}

	public List<AlertResponse> activeForAstronaut(Long astronautId, Long userId) {
		requireAstronautExists(astronautId);
		healthAccessService.requireAccess(astronautId, userId);
		return alertRepository.findAllByAstronautIdAndStatusIn(astronautId, ACTIVE_STATUSES)
				.stream().map(AlertResponse::from).toList();
	}

	@Transactional
	public AlertResponse acknowledge(Long id, Long userId) {
		return transition(id, userId, AlertStatus.ACKNOWLEDGED, AlertHistoryAction.ACKNOWLEDGED);
	}

	@Transactional
	public AlertResponse escalate(Long id, Long userId) {
		return transition(id, userId, AlertStatus.ESCALATED, AlertHistoryAction.ESCALATED);
	}

	@Transactional
	public AlertResponse resolve(Long id, Long userId) {
		return transition(id, userId, AlertStatus.RESOLVED, AlertHistoryAction.RESOLVED);
	}

	@Transactional
	public AlertResponse close(Long id, Long userId) {
		return transition(id, userId, AlertStatus.CLOSED, AlertHistoryAction.CLOSED);
	}

	private AlertResponse transition(
			Long id,
			Long userId,
			AlertStatus target,
			AlertHistoryAction action) {
		Alert alert = getAlert(id);
		UserAccount actor = requireMedicalManager(userId, alert.getAstronaut().getId());
		validateTransition(alert.getStatus(), target);
		Instant now = Instant.now(clock);
		alert.setStatus(target);
		alert.setLastHandledBy(actor);
		switch (target) {
			case ACKNOWLEDGED -> alert.setAcknowledgedAt(now);
			case ESCALATED -> alert.setEscalatedAt(now);
			case RESOLVED -> alert.setResolvedAt(now);
			case CLOSED -> alert.setClosedAt(now);
			case OPEN -> throw new InvalidRequestException("Alerts cannot be reopened through this API.");
		}
		alert = alertRepository.save(alert);
		record(alert, actor, action, "Alert changed to " + target + ".");
		Long alertId = alert.getId();
		userAccountRepository.findByAstronautProfileId(alert.getAstronaut().getId())
				.ifPresent(owner -> notificationService.notifyUser(
						owner.getId(),
						NotificationType.ALERT,
						"Alert status updated",
						"An alert associated with your account has been updated.",
						"alert",
						alertId));
		return AlertResponse.from(alert);
	}

	private void validateTransition(AlertStatus current, AlertStatus target) {
		boolean allowed = switch (target) {
			case ACKNOWLEDGED -> current == AlertStatus.OPEN;
			case ESCALATED -> current == AlertStatus.OPEN || current == AlertStatus.ACKNOWLEDGED;
			case RESOLVED -> ACTIVE_STATUSES.contains(current);
			case CLOSED -> current == AlertStatus.RESOLVED;
			case OPEN -> false;
		};
		if (!allowed) {
			throw new InvalidRequestException(
					"Alert cannot transition from " + current + " to " + target + ".");
		}
	}

	private UserAccount requireMedicalManager(Long userId, Long astronautId) {
		UserAccount actor = userAccountRepository.findById(userId)
				.orElseThrow(() -> new AccessDeniedException("The authenticated account cannot manage alerts."));
		if (actor.getRoles().contains(UserRole.ADMIN)) {
			return actor;
		}
		if (actor.getRoles().contains(UserRole.MEDICAL_OFFICER)
				&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(userId, astronautId)) {
			return actor;
		}
		throw new AccessDeniedException("Only an administrator or assigned medical officer can manage alerts.");
	}

	private Alert getAlert(Long id) {
		return alertRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + id));
	}

	private void requireAstronautExists(Long astronautId) {
		if (!astronautRepository.existsById(astronautId)) {
			throw new ResourceNotFoundException("Astronaut not found with ID: " + astronautId);
		}
	}

	private void record(
			Alert alert,
			UserAccount actor,
			AlertHistoryAction action,
			String details) {
		AlertHistory history = new AlertHistory();
		history.setAlert(alert);
		history.setActor(actor);
		history.setAction(action);
		history.setDetails(details);
		historyRepository.save(history);
	}
}
