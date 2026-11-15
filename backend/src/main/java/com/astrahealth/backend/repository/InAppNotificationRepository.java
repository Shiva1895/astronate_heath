package com.astrahealth.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.astrahealth.backend.entity.InAppNotification;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {

	Page<InAppNotification> findAllByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

	Optional<InAppNotification> findByIdAndRecipientId(Long id, Long recipientId);

	long countByRecipientIdAndReadAtIsNull(Long recipientId);

	@Modifying
	@Query("update InAppNotification notification set notification.readAt = :readAt "
			+ "where notification.recipient.id = :recipientId and notification.readAt is null")
	int markAllRead(@Param("recipientId") Long recipientId, @Param("readAt") java.time.Instant readAt);
}
