package com.astrahealth.backend.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.WellbeingAudioRecording;

public interface WellbeingAudioRecordingRepository extends JpaRepository<WellbeingAudioRecording, Long> {

	Optional<WellbeingAudioRecording> findByIdAndOwnerIdAndReportIsNull(Long id, Long ownerId);

	Optional<WellbeingAudioRecording> findByIdAndOwnerId(Long id, Long ownerId);

	Optional<WellbeingAudioRecording> findByReportId(Long reportId);

	List<WellbeingAudioRecording> findAllByReportIdIn(List<Long> reportIds);
}
