package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.astrahealth.backend.exception.InvalidRequestException;

class PrivateAudioStorageTests {

	@TempDir
	Path storageDirectory;

	@Test
	void validWebmIsStoredOutsideTheWebResourcesAndCanBeDeleted() throws Exception {
		PrivateAudioStorage storage = new PrivateAudioStorage(storageDirectory.toString(), 1024);
		byte[] contents = {(byte) 0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0x01};
		var stored = storage.store(new MockMultipartFile(
				"file",
				"untrusted-name.webm",
				"audio/webm",
				contents));

		assertArrayEquals(contents, storage.read(stored.storageKey()));
		Path storedPath = storageDirectory.resolve(stored.storageKey());
		org.junit.jupiter.api.Assertions.assertTrue(Files.exists(storedPath));
		storage.delete(stored.storageKey());
		assertFalse(Files.exists(storedPath));
	}

	@Test
	void mismatchedTypeSignatureAndOversizedAudioAreRejected() {
		PrivateAudioStorage storage = new PrivateAudioStorage(storageDirectory.toString(), 4);

		assertThrows(
				InvalidRequestException.class,
				() -> storage.store(new MockMultipartFile(
						"file", "not-audio.txt", "text/plain", new byte[] {1, 2, 3, 4})));
		assertThrows(
				InvalidRequestException.class,
				() -> storage.store(new MockMultipartFile(
						"file",
						"invalid.webm",
						"audio/webm",
						new byte[] {1, 2, 3, 4})));
		assertThrows(
				InvalidRequestException.class,
				() -> storage.store(new MockMultipartFile(
						"file",
						"oversized.webm",
						"audio/webm",
						new byte[] {(byte) 0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0x01})));
	}
}
