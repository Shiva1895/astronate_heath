package com.astrahealth.backend.service;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AstronautRepository;

@Service
@Transactional(readOnly = true)
public class AstronautService {

	private final AstronautRepository astronautRepository;

	public AstronautService(AstronautRepository astronautRepository) {
		this.astronautRepository = astronautRepository;
	}

	@Transactional
	public Astronaut createAstronaut(Astronaut astronaut) {
		if (astronautRepository.existsByAstronautCode(astronaut.getAstronautCode())) {
			throw new DuplicateResourceException("Astronaut code already exists.");
		}

		String email = astronaut.getEmail();
		if (email != null && astronautRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("Email already exists.");
		}

		return astronautRepository.save(astronaut);
	}

	public Page<Astronaut> getAllAstronauts(Pageable pageable) {
		return astronautRepository.findAll(pageable);
	}

	public Astronaut getAstronautById(Long id) {
		return astronautRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Astronaut not found with ID: " + id));
	}

	@Transactional
	public Astronaut updateAstronaut(Long id, Astronaut astronautDetails) {
		Astronaut existingAstronaut = getAstronautById(id);

		if (!Objects.equals(existingAstronaut.getAstronautCode(), astronautDetails.getAstronautCode())) {
			ensureCodeIsNotUsedByAnotherAstronaut(astronautDetails.getAstronautCode(), id);
		}
		if (!Objects.equals(existingAstronaut.getEmail(), astronautDetails.getEmail())) {
			ensureEmailIsNotUsedByAnotherAstronaut(astronautDetails.getEmail(), id);
		}

		existingAstronaut.setAstronautCode(astronautDetails.getAstronautCode());
		existingAstronaut.setFullName(astronautDetails.getFullName());
		existingAstronaut.setEmail(astronautDetails.getEmail());
		existingAstronaut.setStatus(astronautDetails.getStatus());

		return astronautRepository.save(existingAstronaut);
	}

	@Transactional
	public void deleteAstronaut(Long id) {
		Astronaut astronaut = getAstronautById(id);
		astronautRepository.delete(astronaut);
	}

	private void ensureCodeIsNotUsedByAnotherAstronaut(String astronautCode, Long currentAstronautId) {
		astronautRepository.findByAstronautCode(astronautCode)
				.filter(astronaut -> !Objects.equals(astronaut.getId(), currentAstronautId))
				.ifPresent(astronaut -> {
					throw new DuplicateResourceException("Astronaut code already exists.");
				});
	}

	private void ensureEmailIsNotUsedByAnotherAstronaut(String email, Long currentAstronautId) {
		if (email == null) {
			return;
		}

		astronautRepository.findByEmail(email)
				.filter(astronaut -> !Objects.equals(astronaut.getId(), currentAstronautId))
				.ifPresent(astronaut -> {
					throw new DuplicateResourceException("Email already exists.");
				});
	}
}
