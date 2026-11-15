package com.astrahealth.backend.service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.astrahealth.backend.exception.InvalidRequestException;

@Component
public class PrivateAudioStorage {

	private final Path storageDirectory;
	private final long maximumBytes;

	public PrivateAudioStorage(
			@Value("${astra.audio.storage-dir:${user.home}/.astra-health/audio}") String storageDirectory,
			@Value("${astra.audio.max-file-size:10485760}") long maximumBytes) {
		this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
		if (maximumBytes < 1) {
			throw new IllegalArgumentException("The audio file size limit must be positive.");
		}
		this.maximumBytes = maximumBytes;
	}

	public StoredAudio store(MultipartFile upload) {
		if (upload == null || upload.isEmpty()) {
			throw new InvalidRequestException("An audio recording is required.");
		}
		if (upload.getSize() > maximumBytes) {
			throw new InvalidRequestException("Audio recording exceeds the configured file size limit.");
		}

		String mediaType = normalizeMediaType(upload.getContentType());
		byte[] contents;
		try {
			contents = upload.getBytes();
		} catch (IOException exception) {
			throw new IllegalStateException("The audio upload could not be read.", exception);
		}
		if (contents.length == 0 || contents.length > maximumBytes || !matchesSignature(mediaType, contents)) {
			throw new InvalidRequestException("The uploaded file is not a supported audio format.");
		}

		String key = UUID.randomUUID().toString();
		Path destination = resolve(key);
		Path temporary = null;
		try {
			Files.createDirectories(storageDirectory);
			temporary = Files.createTempFile(storageDirectory, "upload-", ".part");
			Files.write(temporary, contents);
			try {
				Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temporary, destination);
			}
		} catch (IOException exception) {
			deleteIfPresent(temporary);
			throw new IllegalStateException("Audio could not be stored securely.", exception);
		}
		return new StoredAudio(key, mediaType, contents.length, sha256(contents));
	}

	public byte[] read(String storageKey) {
		try {
			return Files.readAllBytes(resolve(storageKey));
		} catch (IOException exception) {
			throw new IllegalStateException("The stored audio recording could not be read.", exception);
		}
	}

	public void delete(String storageKey) {
		try {
			Files.deleteIfExists(resolve(storageKey));
		} catch (IOException exception) {
			throw new IllegalStateException("The stored audio recording could not be deleted.", exception);
		}
	}

	private Path resolve(String storageKey) {
		if (storageKey == null || !storageKey.matches(
				"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
			throw new IllegalArgumentException("Invalid audio storage key.");
		}
		Path path = storageDirectory.resolve(storageKey).normalize();
		if (!path.getParent().equals(storageDirectory)) {
			throw new IllegalArgumentException("Invalid audio storage key.");
		}
		return path;
	}

	private String normalizeMediaType(String contentType) {
		if (contentType == null) {
			throw new InvalidRequestException("Audio content type is required.");
		}
		String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		if (!mediaType.equals("audio/webm")
				&& !mediaType.equals("audio/ogg")
				&& !mediaType.equals("audio/wav")
				&& !mediaType.equals("audio/x-wav")
				&& !mediaType.equals("audio/mp4")
				&& !mediaType.equals("audio/mpeg")) {
			throw new InvalidRequestException("Only WebM, Ogg, WAV, MP4, or MPEG audio is supported.");
		}
		return mediaType.equals("audio/x-wav") ? "audio/wav" : mediaType;
	}

	private boolean matchesSignature(String mediaType, byte[] bytes) {
		return switch (mediaType) {
			case "audio/webm" -> startsWith(bytes, 0x1A, 0x45, 0xDF, 0xA3);
			case "audio/ogg" -> startsWith(bytes, 'O', 'g', 'g', 'S');
			case "audio/wav", "audio/x-wav" ->
					bytes.length >= 12 && startsWith(bytes, 'R', 'I', 'F', 'F')
							&& startsWithAt(bytes, 8, 'W', 'A', 'V', 'E');
			case "audio/mp4" -> startsWithAt(bytes, 4, 'f', 't', 'y', 'p');
			case "audio/mpeg" -> bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF
					&& ((bytes[1] & 0xE0) == 0xE0);
			default -> false;
		};
	}

	private boolean startsWith(byte[] bytes, int... signature) {
		if (bytes.length < signature.length) {
			return false;
		}
		for (int index = 0; index < signature.length; index++) {
			if ((bytes[index] & 0xFF) != signature[index]) {
				return false;
			}
		}
		return true;
	}

	private boolean startsWithAt(byte[] bytes, int offset, int... signature) {
		if (bytes.length < offset + signature.length) {
			return false;
		}
		for (int index = 0; index < signature.length; index++) {
			if ((bytes[offset + index] & 0xFF) != signature[index]) {
				return false;
			}
		}
		return true;
	}

	private String sha256(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is required for audio integrity checks.", exception);
		}
	}

	private void deleteIfPresent(Path path) {
		if (path == null) {
			return;
		}
		try {
			Files.deleteIfExists(path);
		} catch (IOException exception) {
			throw new IllegalStateException("A temporary audio upload could not be removed.", exception);
		}
	}

	public record StoredAudio(String storageKey, String mediaType, long byteSize, String sha256) {
	}
}
