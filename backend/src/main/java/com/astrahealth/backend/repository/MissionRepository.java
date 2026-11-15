package com.astrahealth.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.astrahealth.backend.entity.Mission;
import com.astrahealth.backend.entity.MissionStatus;

public interface MissionRepository extends JpaRepository<Mission, Long> {

	boolean existsByMissionCodeIgnoreCase(String missionCode);

	Optional<Mission> findByIdAndArchivedAtIsNull(Long id);

	@Query("""
			select distinct mission from Mission mission
			where mission.archivedAt is null
				and (:status is null or mission.status = :status)
				and (:query is null
					or lower(mission.missionCode) like lower(concat('%', :query, '%'))
					or lower(mission.missionName) like lower(concat('%', :query, '%')))
			""")
	Page<Mission> search(
			@Param("query") String query,
			@Param("status") MissionStatus status,
			Pageable pageable);
}
