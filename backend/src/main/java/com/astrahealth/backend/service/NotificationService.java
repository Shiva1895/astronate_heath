package com.astrahealth.backend.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.response.InAppNotificationResponse;
import com.astrahealth.backend.entity.InAppNotification;
import com.astrahealth.backend.entity.NotificationType;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.InAppNotificationRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@Service
@Transactional(readOnly = true)
public class NotificationService {

	private final InAppNotificationRepository notificationRepository;
	private final UserAccountRepository userAccountRepository;
	private final MedicalAssignmentRepository assignmentRepository;
	private final Clock clock;

	public NotificationService(
			InAppNotificationRepository notificationRepository,
			UserAccountRepository userAccountRepository,
			MedicalAssignmentRepository assignmentRepository,
			Clock clock) {
		this.notificationRepository = notificationRepository;
		this.userAccountRepository = userAccountRepository;
		this.assignmentRepository = assignmentRepository;
		this.clock = clock;
	}

	@Transactional
	public void notifyMedicalOfficers(
			Long astronautId,
			NotificationType type,
			String title,
			String genericMessage,
			String resourceType,
			Long resourceId) {
		assignmentRepository.findAllByAstronautId(astronautId).stream()
				.map(assignment -> assignment.getMedicalOfficer().getId())
				.distinct()
				.forEach(userId -> create(userId, type, title, genericMessage, resourceType, resourceId));
	}

	@Transactional
	public void notifyUser(
			Long userId,
			NotificationType type,
			String title,
			String genericMessage,
			String resourceType,
			Long resourceId) {
		create(userId, type, title, genericMessage, resourceType, resourceId);
	}

	public Page<InAppNotificationResponse> list(Long recipientId, Pageable pageable) {
		return notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
				.map(InAppNotificationResponse::from);
	}

	public long unreadCount(Long recipientId) {
		return notificationRepository.countByRecipientIdAndReadAtIsNull(recipientId);
	}

	@Transactional
	public InAppNotificationResponse markRead(Long id, Long recipientId) {
		InAppNotification notification = notificationRepository.findByIdAndRecipientId(id, recipientId)
				.orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
		if (notification.getReadAt() == null) {
			notification.setReadAt(Instant.now(clock));
			notification = notificationRepository.save(notification);
		}
		return InAppNotificationResponse.from(notification);
	}

	@Transactional
	public int markAllRead(Long recipientId) {
		return notificationRepository.markAllRead(recipientId, Instant.now(clock));
	}

	private void create(
			Long recipientId,
			NotificationType type,
			String title,
			String genericMessage,
			String resourceType,
			Long resourceId) {
		UserAccount recipient = userAccountRepository.findById(recipientId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Notification recipient not found with ID: " + recipientId));
		InAppNotification notification = new InAppNotification();
		notification.setRecipient(recipient);
		notification.setType(type);
		notification.setTitle(title);
		notification.setMessage(genericMessage);
		notification.setResourceType(resourceType);
		notification.setResourceId(resourceId);
		notificationRepository.save(notification);
	}
}
