package com.astrahealth.backend.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "missions",
		uniqueConstraints = @UniqueConstraint(name = "uk_missions_code", columnNames = "mission_code"),
		indexes = @Index(name = "idx_missions_status", columnList = "status"))
public class Mission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "mission_code", nullable = false, length = 50)
	private String missionCode;

	@Column(name = "mission_name", nullable = false, length = 150)
	private String missionName;

	@Column(length = 2000)
	private String description;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private MissionStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "archived_at")
	private Instant archivedAt;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "mission_astronauts",
			joinColumns = @JoinColumn(name = "mission_id", nullable = false),
			inverseJoinColumns = @JoinColumn(name = "astronaut_id", nullable = false),
			uniqueConstraints = @UniqueConstraint(
					name = "uk_mission_astronaut",
					columnNames = {"mission_id", "astronaut_id"}),
			indexes = {
					@Index(name = "idx_mission_astronaut_mission", columnList = "mission_id"),
					@Index(name = "idx_mission_astronaut_astronaut", columnList = "astronaut_id")
			})
	private Set<Astronaut> astronauts = new LinkedHashSet<>();

	@PrePersist
	void initializeTimestamps() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void updateTimestamp() {
		updatedAt = Instant.now();
	}

	public Long getId() { return id; }
	public String getMissionCode() { return missionCode; }
	public void setMissionCode(String missionCode) { this.missionCode = missionCode; }
	public String getMissionName() { return missionName; }
	public void setMissionName(String missionName) { this.missionName = missionName; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public LocalDate getStartDate() { return startDate; }
	public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
	public LocalDate getEndDate() { return endDate; }
	public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
	public MissionStatus getStatus() { return status; }
	public void setStatus(MissionStatus status) { this.status = status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	public Instant getArchivedAt() { return archivedAt; }
	public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }
	public Set<Astronaut> getAstronauts() { return astronauts; }
}
