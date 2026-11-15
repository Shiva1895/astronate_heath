package com.astrahealth.backend.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.astrahealth.backend.entity.FollowUpStatus;
import com.astrahealth.backend.entity.MedicalFollowUpAction;

public interface MedicalFollowUpActionRepository extends JpaRepository<MedicalFollowUpAction, Long> {
	Optional<MedicalFollowUpAction> findByIdAndObservationId(Long id, Long observationId);
	List<MedicalFollowUpAction> findAllByObservationIdOrderByCreatedAtDesc(Long observationId);
	List<MedicalFollowUpAction> findAllByObservationAstronautIdOrderByDueDateAsc(Long astronautId);
	Page<MedicalFollowUpAction> findAllByObservationAstronautIdOrderByDueDateAsc(
			Long astronautId,
			Pageable pageable);
	List<MedicalFollowUpAction> findAllByObservationAstronautIdInOrderByDueDateAsc(List<Long> astronautIds);
	List<MedicalFollowUpAction> findAllByOrderByDueDateAsc();

	Page<MedicalFollowUpAction> findAllByStatusOrderByDueDateAsc(FollowUpStatus status, Pageable pageable);

	@Query(
			value = """
					select action from MedicalFollowUpAction action
					where action.observation.astronaut.id in :astronautIds
						and action.status = :status
						and (action.assignedTo is null or action.assignedTo.id = :userId)
					order by action.dueDate asc
					""",
			countQuery = """
					select count(action) from MedicalFollowUpAction action
					where action.observation.astronaut.id in :astronautIds
						and action.status = :status
						and (action.assignedTo is null or action.assignedTo.id = :userId)
					""")
	Page<MedicalFollowUpAction> findAssignedPendingByAstronautIds(
			@Param("astronautIds") List<Long> astronautIds,
			@Param("status") FollowUpStatus status,
			@Param("userId") Long userId,
			Pageable pageable);
}
