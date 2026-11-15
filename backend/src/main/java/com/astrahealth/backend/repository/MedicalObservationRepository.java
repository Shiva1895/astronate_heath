package com.astrahealth.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.MedicalObservation;

public interface MedicalObservationRepository extends JpaRepository<MedicalObservation, Long> {
	Page<MedicalObservation> findAllByAstronautIdOrderByCreatedAtDesc(Long astronautId, Pageable pageable);
}
