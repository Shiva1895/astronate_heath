package com.astrahealth.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.AlertHistory;

public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
	Page<AlertHistory> findAllByAlertIdOrderByOccurredAtDesc(Long alertId, Pageable pageable);
}
