package com.astrahealth.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "wellbeing_audio_recordings", indexes = {
		@Index(name = "idx_wellbeing_audio_owner", columnList = "owner_id"),
		@Index(name = "idx_wellbeing_audio_report", columnList = "report_id")
})
public class WellbeingAudioRecording {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private UserAccount owner;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "report_id", unique = true)
	private WellbeingReport report;

	@Column(name = "storage_key", nullable = false, unique = true, length = 36)
	private String storageKey;

	@Column(name = "media_type", nullable = false, length = 64)
	private String mediaType;

	@Column(name = "byte_size", nullable = false)
	private long byteSize;

	@Column(name = "sha256", nullable = false, length = 64)
	private String sha256;

	@Column(name = "uploaded_at", nullable = false, updatable = false)
	private Instant uploadedAt;

	@PrePersist
	void setUploadedAt() {
		if (uploadedAt == null) {
			uploadedAt = Instant.now();
		}
	}

	public Long getId() { return id; }
	public UserAccount getOwner() { return owner; }
	public void setOwner(UserAccount owner) { this.owner = owner; }
	public WellbeingReport getReport() { return report; }
	public void setReport(WellbeingReport report) { this.report = report; }
	public String getStorageKey() { return storageKey; }
	public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
	public String getMediaType() { return mediaType; }
	public void setMediaType(String mediaType) { this.mediaType = mediaType; }
	public long getByteSize() { return byteSize; }
	public void setByteSize(long byteSize) { this.byteSize = byteSize; }
	public String getSha256() { return sha256; }
	public void setSha256(String sha256) { this.sha256 = sha256; }
	public Instant getUploadedAt() { return uploadedAt; }
}
