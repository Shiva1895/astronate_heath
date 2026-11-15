package com.astrahealth.backend.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.astrahealth.backend.entity.Alert;
import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AlertStatus;
import com.astrahealth.backend.repository.projection.ActiveAlertCount;

public interface AlertRepository extends JpaRepository<Alert, Long> {

	boolean existsBySourceReadingIdAndAnomalyRuleId(Long sourceReadingId, Long anomalyRuleId);

	@Query("""
			select alert from Alert alert
			where (:astronautId is null or alert.astronaut.id = :astronautId)
				and (:status is null or alert.status = :status)
				and (:priority is null or alert.priority = :priority)
			""")
	Page<Alert> search(
			@Param("astronautId") Long astronautId,
			@Param("status") AlertStatus status,
			@Param("priority") AlertPriority priority,
			Pageable pageable);

	Page<Alert> findAllByAstronautIdAndStatusIn(Long astronautId, Collection<AlertStatus> statuses, Pageable pageable);

	List<Alert> findAllByAstronautIdAndStatusIn(Long astronautId, Collection<AlertStatus> statuses);

	long countByAstronautIdAndStatusIn(Long astronautId, Collection<AlertStatus> statuses);

	@Query("""
			select alert.astronaut.id as astronautId, count(alert.id) as alertCount
			from Alert alert
			where alert.astronaut.id in :astronautIds and alert.status in :statuses
			group by alert.astronaut.id
			""")
	List<ActiveAlertCount> countActiveGroupedByAstronaut(
			@Param("astronautIds") List<Long> astronautIds,
			@Param("statuses") Collection<AlertStatus> statuses);
}
