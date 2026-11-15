package com.astrahealth.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.request.MedicalFollowUpRequest;
import com.astrahealth.backend.dto.request.MedicalObservationRequest;
import com.astrahealth.backend.dto.response.MedicalFollowUpResponse;
import com.astrahealth.backend.dto.response.MedicalObservationHistoryResponse;
import com.astrahealth.backend.dto.response.MedicalObservationResponse;
import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.FollowUpStatus;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.MedicalFollowUpAction;
import com.astrahealth.backend.entity.MedicalObservation;
import com.astrahealth.backend.entity.MedicalObservationHistory;
import com.astrahealth.backend.entity.NotificationType;
import com.astrahealth.backend.entity.ObservationHistoryEvent;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AlertRepository;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.MedicalFollowUpActionRepository;
import com.astrahealth.backend.repository.MedicalObservationHistoryRepository;
import com.astrahealth.backend.repository.MedicalObservationRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@Service
@Transactional(readOnly = true)
public class MedicalObservationService {

	private final MedicalObservationRepository observationRepository;
	private final MedicalObservationHistoryRepository historyRepository;
	private final MedicalFollowUpActionRepository followUpRepository;
	private final AstronautRepository astronautRepository;
	private final AlertRepository alertRepository;
	private final HealthReadingRepository readingRepository;
	private final UserAccountRepository userAccountRepository;
	private final MedicalAssignmentRepository assignmentRepository;
	private final NotificationService notificationService;

	public MedicalObservationService(
			MedicalObservationRepository observationRepository,
			MedicalObservationHistoryRepository historyRepository,
			MedicalFollowUpActionRepository followUpRepository,
			AstronautRepository astronautRepository,
			AlertRepository alertRepository,
			HealthReadingRepository readingRepository,
			UserAccountRepository userAccountRepository,
			MedicalAssignmentRepository assignmentRepository,
			NotificationService notificationService) {
		this.observationRepository = observationRepository;
		this.historyRepository = historyRepository;
		this.followUpRepository = followUpRepository;
		this.astronautRepository = astronautRepository;
		this.alertRepository = alertRepository;
		this.readingRepository = readingRepository;
		this.userAccountRepository = userAccountRepository;
		this.assignmentRepository = assignmentRepository;
		this.notificationService = notificationService;
	}

	@Transactional
	public MedicalObservationResponse create(MedicalObservationRequest request, Long userId) {
		if (request == null || request.astronautId() == null) {
			throw new InvalidRequestException("Astronaut ID is required.");
		}
		Astronaut astronaut = getAstronaut(request.astronautId());
		UserAccount actor = requireMedicalAccess(userId, astronaut.getId());
		MedicalObservation observation = new MedicalObservation();
		observation.setAstronaut(astronaut);
		observation.setMedicalOfficer(actor);
		apply(observation, request, astronaut);
		observation = observationRepository.saveAndFlush(observation);
		recordHistory(
				observation,
				actor,
				ObservationHistoryEvent.CREATED,
				observation.getObservationText(),
				"Medical observation created.");
		return MedicalObservationResponse.from(observation);
	}

	@Transactional
	public MedicalObservationResponse update(Long id, MedicalObservationRequest request, Long userId) {
		if (request == null || request.astronautId() == null) {
			throw new InvalidRequestException("Astronaut ID is required.");
		}
		MedicalObservation observation = getObservation(id);
		UserAccount actor = requireMedicalAccess(userId, observation.getAstronaut().getId());
		if (!observation.getAstronaut().getId().equals(request.astronautId())) {
			throw new InvalidRequestException("An observation cannot be moved to another astronaut.");
		}
		apply(observation, request, observation.getAstronaut());
		observation = observationRepository.saveAndFlush(observation);
		recordHistory(
				observation,
				actor,
				ObservationHistoryEvent.UPDATED,
				observation.getObservationText(),
				"Medical observation updated.");
		return MedicalObservationResponse.from(observation);
	}

	public MedicalObservationResponse get(Long id, Long userId) {
		MedicalObservation observation = getObservation(id);
		requireMedicalAccess(userId, observation.getAstronaut().getId());
		return MedicalObservationResponse.from(observation);
	}

	public Page<MedicalObservationResponse> listByAstronaut(
			Long astronautId,
			Long userId,
			Pageable pageable) {
		if (!astronautRepository.existsById(astronautId)) {
			throw new ResourceNotFoundException("Astronaut not found with ID: " + astronautId);
		}
		requireMedicalAccess(userId, astronautId);
		return observationRepository.findAllByAstronautIdOrderByCreatedAtDesc(astronautId, pageable)
				.map(MedicalObservationResponse::from);
	}

	public Page<MedicalObservationHistoryResponse> history(Long id, Long userId, Pageable pageable) {
		MedicalObservation observation = getObservation(id);
		requireMedicalAccess(userId, observation.getAstronaut().getId());
		return historyRepository.findAllByObservationIdOrderByOccurredAtDesc(id, pageable)
				.map(MedicalObservationHistoryResponse::from);
	}

	@Transactional
	public MedicalFollowUpResponse addFollowUp(
			Long observationId,
			MedicalFollowUpRequest request,
			Long userId) {
		if (request == null || request.actionText() == null || request.actionText().isBlank()) {
			throw new InvalidRequestException("Follow-up action text is required.");
		}
		MedicalObservation observation = getObservation(observationId);
		UserAccount actor = requireMedicalAccess(userId, observation.getAstronaut().getId());
		MedicalFollowUpAction action = new MedicalFollowUpAction();
		action.setObservation(observation);
		action.setActionText(request.actionText().trim());
		action.setDueDate(request.dueDate());
		action.setCreatedBy(actor);
		action.setUpdatedBy(actor);
		UserAccount assignee = request.assignedToUserId() == null
				? null
				: requireFollowUpAssignee(request.assignedToUserId(), observation.getAstronaut().getId());
		action.setAssignedTo(assignee);
		action = followUpRepository.saveAndFlush(action);
		recordHistory(
				observation,
				actor,
				ObservationHistoryEvent.FOLLOW_UP_ADDED,
				observation.getObservationText(),
				"Follow-up action " + action.getId() + " added.");
		if (assignee != null) {
			notificationService.notifyUser(
					assignee.getId(),
					NotificationType.FOLLOW_UP,
					"Medical follow-up assigned",
					"A follow-up task has been assigned to you.",
					"medical_follow_up",
					action.getId());
		}
		return MedicalFollowUpResponse.from(action);
	}

	public List<MedicalFollowUpResponse> listFollowUps(Long observationId, Long userId) {
		MedicalObservation observation = getObservation(observationId);
		requireMedicalAccess(userId, observation.getAstronaut().getId());
		return followUpRepository.findAllByObservationIdOrderByCreatedAtDesc(observationId)
				.stream().map(MedicalFollowUpResponse::from).toList();
	}

	@Transactional
	public MedicalFollowUpResponse updateFollowUpStatus(
			Long observationId,
			Long followUpId,
			FollowUpStatus status,
			Long userId) {
		MedicalObservation observation = getObservation(observationId);
		UserAccount actor = requireMedicalAccess(userId, observation.getAstronaut().getId());
		MedicalFollowUpAction action = followUpRepository.findByIdAndObservationId(followUpId, observationId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Follow-up action not found with ID: " + followUpId));
		if (status == null || status == FollowUpStatus.PENDING
				|| (status != FollowUpStatus.COMPLETED && status != FollowUpStatus.CANCELLED)
				|| action.getStatus() != FollowUpStatus.PENDING) {
			throw new InvalidRequestException("Only a pending follow-up can be completed or cancelled.");
		}
		action.setStatus(status);
		action.setUpdatedBy(actor);
		action = followUpRepository.save(action);
		recordHistory(
				observation,
				actor,
				ObservationHistoryEvent.FOLLOW_UP_STATUS_CHANGED,
				observation.getObservationText(),
				"Follow-up action " + action.getId() + " changed to " + status + ".");
		UserAccount notificationRecipient = action.getAssignedTo() == null
				? action.getCreatedBy()
				: action.getAssignedTo();
		if (!notificationRecipient.getId().equals(userId)) {
			notificationService.notifyUser(
					notificationRecipient.getId(),
					NotificationType.FOLLOW_UP,
					"Medical follow-up status updated",
					"An assigned follow-up task has been updated.",
					"medical_follow_up",
					action.getId());
		}
		return MedicalFollowUpResponse.from(action);
	}

	public Page<MedicalFollowUpResponse> followUpsForAssignedAstronauts(Long userId, Pageable pageable) {
		UserAccount actor = userAccountRepository.findById(userId)
				.orElseThrow(() -> new AccessDeniedException("A medical reviewer account is required."));
		Page<MedicalFollowUpAction> actions;
		if (actor.getRoles().contains(UserRole.ADMIN)) {
			actions = followUpRepository.findAllByStatusOrderByDueDateAsc(FollowUpStatus.PENDING, pageable);
		} else if (actor.getRoles().contains(UserRole.MEDICAL_OFFICER)) {
			List<Long> astronautIds = assignmentRepository.findAllByMedicalOfficerId(userId).stream()
					.map(assignment -> assignment.getAstronaut().getId())
					.distinct()
					.toList();
			actions = astronautIds.isEmpty()
					? Page.empty(pageable)
					: followUpRepository.findAssignedPendingByAstronautIds(
							astronautIds, FollowUpStatus.PENDING, userId, pageable);
		} else {
			throw new AccessDeniedException("Only administrators and medical officers can view follow-up tasks.");
		}
		return actions.map(MedicalFollowUpResponse::from);
	}

	private void apply(
			MedicalObservation observation,
			MedicalObservationRequest request,
			Astronaut astronaut) {
		if (request == null || request.observationText() == null || request.observationText().isBlank()) {
			throw new InvalidRequestException("Observation text is required.");
		}
		observation.setObservationText(request.observationText().trim());
		observation.setFollowUpDate(request.followUpDate());
		observation.setRelatedAlert(resolveAlert(request.relatedAlertId(), astronaut));
		observation.setRelatedReading(resolveReading(request.relatedReadingId(), astronaut));
	}

	private Alert resolveAlert(Long alertId, Astronaut astronaut) {
		if (alertId == null) {
			return null;
		}
		Alert alert = alertRepository.findById(alertId)
				.orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));
		if (!alert.getAstronaut().getId().equals(astronaut.getId())) {
			throw new InvalidRequestException("The related alert belongs to a different astronaut.");
		}
		return alert;
	}

	private HealthReading resolveReading(Long readingId, Astronaut astronaut) {
		if (readingId == null) {
			return null;
		}
		HealthReading reading = readingRepository.findById(readingId)
				.orElseThrow(() -> new ResourceNotFoundException("Health reading not found with ID: " + readingId));
		if (!reading.getAstronaut().getId().equals(astronaut.getId())) {
			throw new InvalidRequestException("The related reading belongs to a different astronaut.");
		}
		return reading;
	}

	private Astronaut getAstronaut(Long id) {
		return astronautRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Astronaut not found with ID: " + id));
	}

	private MedicalObservation getObservation(Long id) {
		return observationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Medical observation not found with ID: " + id));
	}

	private UserAccount requireMedicalAccess(Long userId, Long astronautId) {
		UserAccount actor = userAccountRepository.findById(userId)
				.orElseThrow(() -> new AccessDeniedException(
						"The authenticated account cannot access medical observations."));
		if (actor.getRoles().contains(UserRole.ADMIN)) {
			return actor;
		}

		if (actor.getRoles().contains(UserRole.MEDICAL_OFFICER)
				&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(userId, astronautId)) {
			return actor;
		}
		throw new AccessDeniedException(
				"Only an administrator or assigned medical officer can access medical observations.");
	}

	private UserAccount requireFollowUpAssignee(Long assigneeId, Long astronautId) {
		UserAccount assignee = userAccountRepository.findById(assigneeId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Follow-up assignee not found with ID: " + assigneeId));
		if (assignee.getRoles().contains(UserRole.ADMIN)
				|| (assignee.getRoles().contains(UserRole.MEDICAL_OFFICER)
						&& assignmentRepository.existsByMedicalOfficerIdAndAstronautId(
								assigneeId, astronautId))) {
			return assignee;
		}
		throw new InvalidRequestException("The follow-up assignee is not authorized for this astronaut.");
	}

	private void recordHistory(
			MedicalObservation observation,
			UserAccount actor,
			ObservationHistoryEvent event,
			String textSnapshot,
			String details) {
		MedicalObservationHistory history = new MedicalObservationHistory();
		history.setObservation(observation);
		history.setActor(actor);
		history.setEvent(event);
		history.setObservationTextSnapshot(textSnapshot);
		history.setFollowUpDateSnapshot(observation.getFollowUpDate());
		history.setRelatedAlertIdSnapshot(
				observation.getRelatedAlert() == null ? null : observation.getRelatedAlert().getId());
		history.setRelatedReadingIdSnapshot(
				observation.getRelatedReading() == null ? null : observation.getRelatedReading().getId());
		history.setDetails(details);
		historyRepository.save(history);
	}
}
