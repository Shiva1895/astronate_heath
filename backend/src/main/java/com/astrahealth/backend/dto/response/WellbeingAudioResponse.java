package com.astrahealth.backend.dto.response;

import java.time.Instant;

import com.astrahealth.backend.entity.WellbeingAudioRecording;

public record WellbeingAudioResponse(
		Long id,
		String mediaType,
		long byteSize,
		Instant uploadedAt) {

	public static WellbeingAudioResponse from(WellbeingAudioRecording recording) {
		return new WellbeingAudioResponse(
				recording.getId(),
				recording.getMediaType(),
				recording.getByteSize(),
				recording.getUploadedAt());
	}
}
