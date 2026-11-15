package com.astrahealth.backend.service;

public interface TranscriptionProvider {

	String transcribe(byte[] audio, String mediaType);
}
