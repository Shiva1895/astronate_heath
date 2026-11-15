package com.astrahealth.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.response.AstronautResponse;
import com.astrahealth.backend.dto.response.MedicalAssignmentResponse;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.MedicalAssignment;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.entity.UserRole;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.MedicalAssignmentRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@Service
@Transactional(readOnly = true)
public class MedicalAssignmentService {

	private final MedicalAssignmentRepository assignmentRepository;
	private final UserAccountRepository userAccountRepository;
	private final AstronautRepository astronautRepository;

	public MedicalAssignmentService(
			MedicalAssignmentRepository assignmentRepository,
			UserAccountRepository userAccountRepository,
			AstronautRepository astronautRepository) {
		this.assignmentRepository = assignmentRepository;
		this.userAccountRepository = userAccountRepository;
		this.astronautRepository = astronautRepository;
	}

	@Transactional
	public MedicalAssignmentResponse assign(Long medicalOfficerId, Long astronautId) {
		UserAccount officer = userAccountRepository.findById(medicalOfficerId)
				.orElseThrow(() -> new ResourceNotFoundException("User account not found with ID: " + medicalOfficerId));
		if (!officer.getRoles().contains(UserRole.MEDICAL_OFFICER)) {
			throw new InvalidRequestException("The selected user is not a medical officer.");
		}
		Astronaut astronaut = astronautRepository.findById(astronautId)
				.orElseThrow(() -> new ResourceNotFoundException("Astronaut not found with ID: " + astronautId));
		if (assignmentRepository.existsByMedicalOfficerIdAndAstronautId(medicalOfficerId, astronautId)) {
			throw new DuplicateResourceException("This medical officer is already assigned to the astronaut.");
		}

		MedicalAssignment assignment = new MedicalAssignment();
		assignment.setMedicalOfficer(officer);
		assignment.setAstronaut(astronaut);
		return MedicalAssignmentResponse.from(assignmentRepository.save(assignment));
	}

	@Transactional
	public void remove(Long assignmentId) {
		MedicalAssignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Medical assignment not found with ID: " + assignmentId));
		assignmentRepository.delete(assignment);
	}

	public Page<MedicalAssignmentResponse> listAssignments(Pageable pageable) {
		return assignmentRepository.findAll(pageable).map(MedicalAssignmentResponse::from);
	}

	public Page<AstronautResponse> getAssignedAstronauts(Long medicalOfficerId, Pageable pageable) {
		return assignmentRepository.findAllByMedicalOfficerId(medicalOfficerId, pageable)
				.map(MedicalAssignment::getAstronaut)
				.map(astronaut -> new AstronautResponse(
						astronaut.getId(),
						astronaut.getAstronautCode(),
						astronaut.getFullName(),
						astronaut.getEmail(),
						astronaut.getStatus()));
	}

	public boolean isAssigned(Long medicalOfficerId, Long astronautId) {
		return assignmentRepository.existsByMedicalOfficerIdAndAstronautId(medicalOfficerId, astronautId);
	}
}
