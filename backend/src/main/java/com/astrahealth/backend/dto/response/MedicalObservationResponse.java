package com.astrahealth.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.astrahealth.backend.entity.MedicalObservation;

public record MedicalObservationResponse(
		Long id,
		Long astronautId,
		Long medicalOfficerUserId,
		String observationText,
		Long relatedAlertId,
		Long relatedReadingId,
		LocalDate followUpDate,
		Instant createdAt,
		Instant updatedAt) {

	public static MedicalObservationResponse from(MedicalObservation observation) {
		return new MedicalObservationResponse(
				observation.getId(),
				observation.getAstronaut().getId(),
				observation.getMedicalOfficer().getId(),
				observation.getObservationText(),
				observation.getRelatedAlert() == null ? null : observation.getRelatedAlert().getId(),
				observation.getRelatedReading() == null ? null : observation.getRelatedReading().getId(),
				observation.getFollowUpDate(),
				observation.getCreatedAt(),
				observation.getUpdatedAt());
	}
}
