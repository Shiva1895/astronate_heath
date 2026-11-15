package com.astrahealth.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.MedicalObservationHistory;

public interface MedicalObservationHistoryRepository extends JpaRepository<MedicalObservationHistory, Long> {
	Page<MedicalObservationHistory> findAllByObservationIdOrderByOccurredAtDesc(
			Long observationId,
			Pageable pageable);
}
