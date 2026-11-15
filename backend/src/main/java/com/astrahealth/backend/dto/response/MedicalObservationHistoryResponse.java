package com.astrahealth.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.astrahealth.backend.entity.MedicalObservationHistory;
import com.astrahealth.backend.entity.ObservationHistoryEvent;

public record MedicalObservationHistoryResponse(
		Long id,
		ObservationHistoryEvent event,
		Long actorUserId,
		String observationTextSnapshot,
		LocalDate followUpDateSnapshot,
		Long relatedAlertIdSnapshot,
		Long relatedReadingIdSnapshot,
		String details,
		Instant occurredAt) {

	public static MedicalObservationHistoryResponse from(MedicalObservationHistory history) {
		return new MedicalObservationHistoryResponse(
				history.getId(),
				history.getEvent(),
				history.getActor().getId(),
				history.getObservationTextSnapshot(),
				history.getFollowUpDateSnapshot(),
				history.getRelatedAlertIdSnapshot(),
				history.getRelatedReadingIdSnapshot(),
				history.getDetails(),
				history.getOccurredAt());
	}
}
