package com.astrahealth.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.Astronaut;

public interface AstronautRepository extends JpaRepository<Astronaut, Long> {

	Optional<Astronaut> findByAstronautCode(String astronautCode);

	Optional<Astronaut> findByEmail(String email);

	boolean existsByAstronautCode(String astronautCode);

	boolean existsByEmail(String email);
}
