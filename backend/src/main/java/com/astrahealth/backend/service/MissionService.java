package com.astrahealth.backend.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.astrahealth.backend.dto.request.MissionRequest;
import com.astrahealth.backend.dto.response.MissionResponse;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.Mission;
import com.astrahealth.backend.entity.MissionStatus;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.MissionRepository;

@Service
@Transactional(readOnly = true)
public class MissionService {

	private final MissionRepository missionRepository;
	private final AstronautRepository astronautRepository;

	public MissionService(MissionRepository missionRepository, AstronautRepository astronautRepository) {
		this.missionRepository = missionRepository;
		this.astronautRepository = astronautRepository;
	}

	@Transactional
	public MissionResponse create(MissionRequest request) {
		String code = request.missionCode().trim().toUpperCase(Locale.ROOT);
		if (missionRepository.existsByMissionCodeIgnoreCase(code)) {
			throw new DuplicateResourceException("Mission code already exists.");
		}
		validateDates(request.startDate(), request.endDate());
		Mission mission = new Mission();
		apply(mission, request, code);
		return MissionResponse.from(missionRepository.save(mission));
	}

	@Transactional
	public MissionResponse update(Long id, MissionRequest request) {
		Mission mission = getMission(id);
		String code = request.missionCode().trim().toUpperCase(Locale.ROOT);
		if (!code.equalsIgnoreCase(mission.getMissionCode())
				&& missionRepository.existsByMissionCodeIgnoreCase(code)) {
			throw new DuplicateResourceException("Mission code already exists.");
		}
		validateDates(request.startDate(), request.endDate());
		apply(mission, request, code);
		return MissionResponse.from(missionRepository.save(mission));
	}

	public MissionResponse get(Long id) {
		return MissionResponse.from(getMission(id));
	}

	public Page<MissionResponse> search(
			String query,
			MissionStatus status,
			Pageable pageable) {
		String normalizedQuery = StringUtils.hasText(query) ? query.trim() : null;
		return missionRepository.search(normalizedQuery, status, pageable).map(MissionResponse::from);
	}

	@Transactional
	public MissionResponse assignAstronaut(Long missionId, Long astronautId) {
		Mission mission = getMission(missionId);
		if (mission.getStatus() == MissionStatus.CANCELLED || mission.getStatus() == MissionStatus.COMPLETED) {
			throw new InvalidRequestException("Astronauts cannot be assigned to a completed or cancelled mission.");
		}
		Astronaut astronaut = astronautRepository.findById(astronautId)
				.orElseThrow(() -> new ResourceNotFoundException("Astronaut not found with ID: " + astronautId));
		mission.getAstronauts().add(astronaut);
		return MissionResponse.from(missionRepository.save(mission));
	}

	@Transactional
	public MissionResponse removeAstronaut(Long missionId, Long astronautId) {
		Mission mission = getMission(missionId);
		Astronaut astronaut = astronautRepository.findById(astronautId)
				.orElseThrow(() -> new ResourceNotFoundException("Astronaut not found with ID: " + astronautId));
		mission.getAstronauts().remove(astronaut);
		return MissionResponse.from(missionRepository.save(mission));
	}

	@Transactional
	public void archive(Long id) {
		Mission mission = getMission(id);
		if (mission.getArchivedAt() == null) {
			mission.setArchivedAt(Instant.now());
			missionRepository.save(mission);
		}
	}

	private Mission getMission(Long id) {
		return missionRepository.findByIdAndArchivedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + id));
	}

	private void apply(Mission mission, MissionRequest request, String code) {
		mission.setMissionCode(code);
		mission.setMissionName(request.missionName().trim());
		mission.setDescription(request.description());
		mission.setStartDate(request.startDate());
		mission.setEndDate(request.endDate());
		mission.setStatus(request.status());
	}

	private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
		if (endDate.isBefore(startDate)) {
			throw new InvalidRequestException("Mission end date must be on or after its start date.");
		}
	}
}
