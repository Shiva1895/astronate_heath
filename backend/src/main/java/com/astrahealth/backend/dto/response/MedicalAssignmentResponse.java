package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.MedicalAssignment;

public record MedicalAssignmentResponse(
		Long id,
		Long medicalOfficerUserId,
		String medicalOfficerEmail,
		Long astronautId,
		String astronautCode,
		Instant createdAt) {

	public static MedicalAssignmentResponse from(MedicalAssignment assignment) {
		return new MedicalAssignmentResponse(
				assignment.getId(),
				assignment.getMedicalOfficer().getId(),
				assignment.getMedicalOfficer().getEmail(),
				assignment.getAstronaut().getId(),
				assignment.getAstronaut().getAstronautCode(),
				assignment.getCreatedAt());
	}
}
