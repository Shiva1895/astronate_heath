package com.astrahealth.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.astrahealth.backend.entity.MedicalAssignment;

public interface MedicalAssignmentRepository extends JpaRepository<MedicalAssignment, Long> {

	@Override
	@EntityGraph(attributePaths = {"medicalOfficer", "astronaut"})
	Page<MedicalAssignment> findAll(Pageable pageable);

	boolean existsByMedicalOfficerIdAndAstronautId(Long medicalOfficerId, Long astronautId);

	boolean existsByMedicalOfficerIdAndAstronautIdAndIdNot(
			Long medicalOfficerId,
			Long astronautId,
			Long assignmentId);

	@EntityGraph(attributePaths = "astronaut")
	Page<MedicalAssignment> findAllByMedicalOfficerId(Long medicalOfficerId, Pageable pageable);

	List<MedicalAssignment> findAllByAstronautId(Long astronautId);

	List<MedicalAssignment> findAllByMedicalOfficerId(Long medicalOfficerId);

}
