package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.astrahealth.backend.entity.InAppNotification;
import com.astrahealth.backend.entity.NotificationType;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.InAppNotificationRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

	@Mock
	private InAppNotificationRepository notificationRepository;
	@Mock
	private UserAccountRepository userAccountRepository;
	@Mock
	private MedicalAssignmentRepository assignmentRepository;

	private NotificationService notificationService;

	@BeforeEach
	void setUp() {
		notificationService = new NotificationService(
				notificationRepository,
				userAccountRepository,
				assignmentRepository,
				Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneOffset.UTC));
	}

	@Test
	void notificationHistoryAndUnreadCountAreScopedToRecipient() {
		InAppNotification notification = notification();
		when(notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(4L, PageRequest.of(0, 10)))
				.thenReturn(new PageImpl<>(List.of(notification)));
		when(notificationRepository.countByRecipientIdAndReadAtIsNull(4L)).thenReturn(1L);

		var page = notificationService.list(4L, PageRequest.of(0, 10));

		assertEquals(1, page.getTotalElements());
		assertEquals(1L, notificationService.unreadCount(4L));
		verify(notificationRepository).findAllByRecipientIdOrderByCreatedAtDesc(4L, PageRequest.of(0, 10));
	}

	@Test
	void onlyTheNotificationRecipientCanMarkItRead() {
		InAppNotification notification = notification();
		when(notificationRepository.findByIdAndRecipientId(8L, 4L)).thenReturn(Optional.of(notification));
		when(notificationRepository.save(notification)).thenReturn(notification);

		var response = notificationService.markRead(8L, 4L);

		assertEquals(Instant.parse("2026-11-15T12:00:00Z"), response.readAt());
		verify(notificationRepository).save(notification);
		assertThrows(
				ResourceNotFoundException.class,
				() -> notificationService.markRead(8L, 5L));
	}

	@Test
	void missingNotificationRecipientIsNotSilentlyIgnored() {
		when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> notificationService.notifyUser(
						99L,
						NotificationType.SYSTEM,
						"System update",
						"A system update is available.",
						"system",
						null));
	}

	private InAppNotification notification() {
		InAppNotification notification = new InAppNotification();
		notification.setRecipient(new UserAccount());
		notification.setType(NotificationType.ALERT);
		notification.setTitle("Alert updated");
		notification.setMessage("An alert was updated.");
		return notification;
	}
}
