package com.astrahealth.backend.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "wellbeing_reports", indexes = {
		@Index(name = "idx_wellbeing_owner_submitted", columnList = "submitter_id, submitted_at"),
		@Index(name = "idx_wellbeing_astronaut_submitted", columnList = "astronaut_id, submitted_at"),
		@Index(name = "idx_wellbeing_status_submitted", columnList = "status, submitted_at")
})
public class WellbeingReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "astronaut_id", nullable = false)
	private Astronaut astronaut;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "submitter_id", nullable = false)
	private UserAccount submitter;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WellbeingCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WellbeingReportStatus status = WellbeingReportStatus.SUBMITTED;

	@Column(name = "report_text", length = 10000)
	private String reportText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewed_by_user_id")
	private UserAccount reviewedBy;

	@Column(name = "review_notes", length = 2000)
	private String reviewNotes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "follow_up_assignee_id")
	private UserAccount followUpAssignee;

	@Column(name = "follow_up_due_date")
	private LocalDate followUpDueDate;

	@Column(name = "submitted_at", nullable = false, updatable = false)
	private Instant submittedAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "reviewed_at")
	private Instant reviewedAt;

	@PrePersist
	void initializeTimestamps() {
		Instant now = Instant.now();
		submittedAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void updateTimestamp() {
		updatedAt = Instant.now();
	}

	public Long getId() { return id; }
	public Astronaut getAstronaut() { return astronaut; }
	public void setAstronaut(Astronaut astronaut) { this.astronaut = astronaut; }
	public UserAccount getSubmitter() { return submitter; }
	public void setSubmitter(UserAccount submitter) { this.submitter = submitter; }
	public WellbeingCategory getCategory() { return category; }
	public void setCategory(WellbeingCategory category) { this.category = category; }
	public WellbeingReportStatus getStatus() { return status; }
	public void setStatus(WellbeingReportStatus status) { this.status = status; }
	public String getReportText() { return reportText; }
	public void setReportText(String reportText) { this.reportText = reportText; }
	public UserAccount getReviewedBy() { return reviewedBy; }
	public void setReviewedBy(UserAccount reviewedBy) { this.reviewedBy = reviewedBy; }
	public String getReviewNotes() { return reviewNotes; }
	public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
	public UserAccount getFollowUpAssignee() { return followUpAssignee; }
	public void setFollowUpAssignee(UserAccount followUpAssignee) { this.followUpAssignee = followUpAssignee; }
	public LocalDate getFollowUpDueDate() { return followUpDueDate; }
	public void setFollowUpDueDate(LocalDate followUpDueDate) { this.followUpDueDate = followUpDueDate; }
	public Instant getSubmittedAt() { return submittedAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	public Instant getReviewedAt() { return reviewedAt; }
	public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}
