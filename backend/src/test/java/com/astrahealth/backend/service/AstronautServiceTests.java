package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AstronautRepository;

@ExtendWith(MockitoExtension.class)
class AstronautServiceTests {

	private static final Long ASTRONAUT_ID = 1L;

	@Mock
	private AstronautRepository astronautRepository;

	@InjectMocks
	private AstronautService astronautService;

	@Test
	void createSavesAstronautWhenCodeAndEmailAreUnique() {
		Astronaut astronaut = createAstronaut(null, "AST-001", "pilot@example.com");
		when(astronautRepository.existsByAstronautCode("AST-001")).thenReturn(false);
		when(astronautRepository.existsByEmail("pilot@example.com")).thenReturn(false);
		when(astronautRepository.save(astronaut)).thenReturn(astronaut);

		Astronaut createdAstronaut = astronautService.createAstronaut(astronaut);

		assertEquals(astronaut, createdAstronaut);
		verify(astronautRepository).save(astronaut);
	}

	@Test
	void createRejectsDuplicateAstronautCode() {
		Astronaut astronaut = createAstronaut(null, "AST-001", "pilot@example.com");
		when(astronautRepository.existsByAstronautCode("AST-001")).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> astronautService.createAstronaut(astronaut));

		verify(astronautRepository, never()).save(any(Astronaut.class));
	}

	@Test
	void createRejectsDuplicateEmail() {
		Astronaut astronaut = createAstronaut(null, "AST-001", "pilot@example.com");
		when(astronautRepository.existsByAstronautCode("AST-001")).thenReturn(false);
		when(astronautRepository.existsByEmail("pilot@example.com")).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> astronautService.createAstronaut(astronaut));

		verify(astronautRepository, never()).save(any(Astronaut.class));
	}

	@Test
	void updateAllowsAstronautToKeepItsExistingCodeAndEmail() {
		Astronaut existingAstronaut = createAstronaut(ASTRONAUT_ID, "AST-001", "pilot@example.com");
		Astronaut astronautDetails = createAstronaut(null, "AST-001", "pilot@example.com");
		astronautDetails.setFullName("Updated Name");
		when(astronautRepository.findById(ASTRONAUT_ID)).thenReturn(Optional.of(existingAstronaut));
		when(astronautRepository.save(existingAstronaut)).thenReturn(existingAstronaut);

		Astronaut updatedAstronaut = astronautService.updateAstronaut(ASTRONAUT_ID, astronautDetails);

		assertEquals(ASTRONAUT_ID, updatedAstronaut.getId());
		assertEquals("AST-001", updatedAstronaut.getAstronautCode());
		assertEquals("Updated Name", updatedAstronaut.getFullName());
		assertEquals("pilot@example.com", updatedAstronaut.getEmail());
		verify(astronautRepository, never()).findByAstronautCode("AST-001");
		verify(astronautRepository, never()).findByEmail("pilot@example.com");
		verify(astronautRepository).save(existingAstronaut);
	}

	@Test
	void updateAllowsANewUniqueEmailAndPreservesDatabaseId() {
		Astronaut existingAstronaut = createAstronaut(ASTRONAUT_ID, "AST-001", "pilot@example.com");
		Astronaut astronautDetails = createAstronaut(null, "AST-001", "updated@example.com");
		when(astronautRepository.findById(ASTRONAUT_ID)).thenReturn(Optional.of(existingAstronaut));
		when(astronautRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
		when(astronautRepository.save(existingAstronaut)).thenReturn(existingAstronaut);

		Astronaut updatedAstronaut = astronautService.updateAstronaut(ASTRONAUT_ID, astronautDetails);

		assertEquals(ASTRONAUT_ID, updatedAstronaut.getId());
		assertEquals("AST-001", updatedAstronaut.getAstronautCode());
		assertEquals("updated@example.com", updatedAstronaut.getEmail());
		verify(astronautRepository).findByEmail("updated@example.com");
		verify(astronautRepository).save(existingAstronaut);
	}

	@Test
	void updateRejectsCodeOwnedByAnotherAstronaut() {
		Astronaut existingAstronaut = createAstronaut(ASTRONAUT_ID, "AST-001", "pilot@example.com");
		Astronaut otherAstronaut = createAstronaut(2L, "AST-002", "other@example.com");
		Astronaut astronautDetails = createAstronaut(null, "AST-002", "pilot@example.com");
		when(astronautRepository.findById(ASTRONAUT_ID)).thenReturn(Optional.of(existingAstronaut));
		when(astronautRepository.findByAstronautCode("AST-002")).thenReturn(Optional.of(otherAstronaut));

		assertThrows(
				DuplicateResourceException.class,
				() -> astronautService.updateAstronaut(ASTRONAUT_ID, astronautDetails));
		verify(astronautRepository, never()).save(existingAstronaut);
	}

	@Test
	void updateRejectsEmailOwnedByAnotherAstronaut() {
		Astronaut existingAstronaut = createAstronaut(ASTRONAUT_ID, "AST-001", "pilot@example.com");
		Astronaut otherAstronaut = createAstronaut(2L, "AST-002", "other@example.com");
		Astronaut astronautDetails = createAstronaut(null, "AST-001", "other@example.com");
		when(astronautRepository.findById(ASTRONAUT_ID)).thenReturn(Optional.of(existingAstronaut));
		when(astronautRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherAstronaut));

		assertThrows(
				DuplicateResourceException.class,
				() -> astronautService.updateAstronaut(ASTRONAUT_ID, astronautDetails));
		verify(astronautRepository, never()).save(existingAstronaut);
	}

	@Test
	void getAstronautByIdThrowsWhenAstronautDoesNotExist() {
		when(astronautRepository.findById(ASTRONAUT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> astronautService.getAstronautById(ASTRONAUT_ID));
	}

	@Test
	void updateThrowsWhenAstronautDoesNotExist() {
		when(astronautRepository.findById(ASTRONAUT_ID)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> astronautService.updateAstronaut(ASTRONAUT_ID, createAstronaut(null, "AST-001", "pilot@example.com")));
		verify(astronautRepository, never()).save(any(Astronaut.class));
	}

	@Test
	void deleteRemovesOnlyTheRequestedAstronaut() {
		Astronaut astronaut = createAstronaut(ASTRONAUT_ID, "AST-001", "pilot@example.com");
		when(astronautRepository.findById(ASTRONAUT_ID)).thenReturn(Optional.of(astronaut));

		astronautService.deleteAstronaut(ASTRONAUT_ID);

		verify(astronautRepository).findById(ASTRONAUT_ID);
		verify(astronautRepository).delete(astronaut);
	}

	@Test
	void deleteDoesNotRemoveAnythingWhenAstronautDoesNotExist() {
		when(astronautRepository.findById(ASTRONAUT_ID)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> astronautService.deleteAstronaut(ASTRONAUT_ID));

		verify(astronautRepository, never()).delete(any(Astronaut.class));
	}

	private Astronaut createAstronaut(Long id, String astronautCode, String email) {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(id);
		astronaut.setAstronautCode(astronautCode);
		astronaut.setFullName("Test Astronaut");
		astronaut.setEmail(email);
		astronaut.setStatus("ACTIVE");
		return astronaut;
	}
}
