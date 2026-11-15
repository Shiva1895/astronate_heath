package com.astrahealth.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

	Page<AuditEvent> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
