package com.astrahealth.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.WellbeingReport;
import com.astrahealth.backend.entity.WellbeingReportStatus;

public interface WellbeingReportRepository extends JpaRepository<WellbeingReport, Long> {

	Page<WellbeingReport> findAllByOrderBySubmittedAtAsc(Pageable pageable);

	Page<WellbeingReport> findAllBySubmitterIdOrderBySubmittedAtDesc(Long submitterId, Pageable pageable);

	Page<WellbeingReport> findAllByAstronautIdOrderBySubmittedAtDesc(Long astronautId, Pageable pageable);

	Page<WellbeingReport> findAllByAstronautIdAndStatusOrderBySubmittedAtDesc(
			Long astronautId,
			WellbeingReportStatus status,
			Pageable pageable);

	Page<WellbeingReport> findAllByStatusOrderBySubmittedAtAsc(
			WellbeingReportStatus status,
			Pageable pageable);

	Page<WellbeingReport> findAllByAstronautIdInAndStatusOrderBySubmittedAtAsc(
			List<Long> astronautIds,
			WellbeingReportStatus status,
			Pageable pageable);

	Page<WellbeingReport> findAllByAstronautIdInOrderBySubmittedAtAsc(
			List<Long> astronautIds,
			Pageable pageable);
}
