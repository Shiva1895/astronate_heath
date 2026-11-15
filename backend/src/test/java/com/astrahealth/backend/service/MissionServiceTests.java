package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.astrahealth.backend.dto.request.MissionRequest;
import com.astrahealth.backend.entity.Mission;
import com.astrahealth.backend.entity.MissionStatus;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.MissionRepository;

@ExtendWith(MockitoExtension.class)
class MissionServiceTests {

	@Mock
	private MissionRepository missionRepository;

	@Mock
	private AstronautRepository astronautRepository;

	@InjectMocks
	private MissionService missionService;

	@Test
	void duplicateMissionCodeIsRejected() {
		when(missionRepository.existsByMissionCodeIgnoreCase("ORBIT-1")).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> missionService.create(request()));
		verify(missionRepository, never()).save(any(Mission.class));
	}

	@Test
	void invalidDateRangeIsRejected() {
		when(missionRepository.existsByMissionCodeIgnoreCase("ORBIT-1")).thenReturn(false);
		MissionRequest invalid = new MissionRequest(
				"ORBIT-1", "Orbital Mission", null,
				LocalDate.parse("2027-02-01"), LocalDate.parse("2027-01-01"), MissionStatus.PLANNED);

		assertThrows(InvalidRequestException.class, () -> missionService.create(invalid));
		verify(missionRepository, never()).save(any(Mission.class));
	}

	@Test
	void deleteArchivesMissionInsteadOfDeletingIt() {
		Mission mission = new Mission();
		when(missionRepository.findByIdAndArchivedAtIsNull(1L)).thenReturn(Optional.of(mission));
		when(missionRepository.save(mission)).thenReturn(mission);

		missionService.archive(1L);

		assertNotNull(mission.getArchivedAt());
		verify(missionRepository).save(mission);
		verify(missionRepository, never()).delete(any(Mission.class));
	}

	@Test
	void missionCanBeCreatedWhenCodeIsUnique() {
		when(missionRepository.existsByMissionCodeIgnoreCase("ORBIT-1")).thenReturn(false);
		when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> {
			Mission mission = invocation.getArgument(0);
			mission.setMissionCode("ORBIT-1");
			return mission;
		});

		var created = missionService.create(request());

		assertEquals("ORBIT-1", created.missionCode());
		verify(missionRepository).save(any(Mission.class));
	}

	@Test
	void missionCanAssignAndRemoveAnAstronaut() {
		Mission mission = new Mission();
		mission.setMissionCode("ORBIT-1");
		mission.setMissionName("Orbital Mission");
		mission.setStartDate(LocalDate.parse("2027-01-01"));
		mission.setEndDate(LocalDate.parse("2027-02-01"));
		mission.setStatus(MissionStatus.PLANNED);
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		astronaut.setAstronautCode("AST-007");
		astronaut.setFullName("Test Astronaut");
		astronaut.setStatus("ACTIVE");
		when(missionRepository.findByIdAndArchivedAtIsNull(1L)).thenReturn(Optional.of(mission));
		when(astronautRepository.findById(7L)).thenReturn(Optional.of(astronaut));
		when(missionRepository.save(mission)).thenReturn(mission);

		missionService.assignAstronaut(1L, 7L);
		assertEquals(1, mission.getAstronauts().size());

		missionService.removeAstronaut(1L, 7L);
		assertEquals(0, mission.getAstronauts().size());
	}

	private MissionRequest request() {
		return new MissionRequest(
				"ORBIT-1", "Orbital Mission", "Test mission",
				LocalDate.parse("2027-01-01"), LocalDate.parse("2027-02-01"), MissionStatus.PLANNED);
	}
}
