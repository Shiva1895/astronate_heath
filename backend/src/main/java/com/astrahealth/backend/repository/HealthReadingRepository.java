package com.astrahealth.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.repository.projection.DailyHealthAggregate;

public interface HealthReadingRepository extends JpaRepository<HealthReading, Long> {

	boolean existsBySourceEventId(String sourceEventId);

	Page<HealthReading> findAllByAstronautId(Long astronautId, Pageable pageable);

	@Query("""
			select reading from HealthReading reading
			where reading.astronaut.id = :astronautId
				and (:readingType is null or reading.readingType = :readingType)
				and (:fromTime is null or reading.recordedAt >= :fromTime)
				and (:toTime is null or reading.recordedAt < :toTime)
			""")
	Page<HealthReading> search(
			@Param("astronautId") Long astronautId,
			@Param("readingType") ReadingType readingType,
			@Param("fromTime") Instant fromTime,
			@Param("toTime") Instant toTime,
			Pageable pageable);

	Optional<HealthReading> findFirstByAstronautIdAndReadingTypeOrderByRecordedAtDescIdDesc(
			Long astronautId,
			ReadingType readingType);

	@Query("""
			select reading from HealthReading reading
			where reading.astronaut.id in :astronautIds
				and not exists (
					select newer.id from HealthReading newer
					where newer.astronaut.id = reading.astronaut.id
						and newer.readingType = reading.readingType
						and (newer.recordedAt > reading.recordedAt
							or (newer.recordedAt = reading.recordedAt and newer.id > reading.id)))
			""")
	List<HealthReading> findLatestForAstronauts(@Param("astronautIds") List<Long> astronautIds);

	List<HealthReading> findTop100ByAstronautIdAndReadingTypeAndRecordedAtLessThanOrderByRecordedAtDescIdDesc(
			Long astronautId,
			ReadingType readingType,
			Instant recordedAt,
			Pageable pageable);

	@Query(value = """
			WITH filtered AS (
				SELECT id, CAST(recorded_at AS DATE) AS bucket_date, reading_type,
					reading_value, recorded_at
				FROM health_readings
				WHERE astronaut_id = :astronautId
					AND recorded_at >= :fromTime
					AND recorded_at < :toTime
					AND (:readingType IS NULL OR reading_type = :readingType)
			),
			aggregates AS (
				SELECT bucket_date, reading_type,
					MIN(reading_value) AS minimum_value,
					MAX(reading_value) AS maximum_value,
					AVG(reading_value) AS average_value,
					COUNT(*) AS reading_count,
					MAX(recorded_at) AS latest_recorded_at
				FROM filtered
				GROUP BY bucket_date, reading_type
			),
			ranked AS (
				SELECT bucket_date, reading_type, reading_value,
					ROW_NUMBER() OVER (
						PARTITION BY bucket_date, reading_type
						ORDER BY recorded_at DESC, id DESC) AS row_number
				FROM filtered
			)
			SELECT aggregates.bucket_date AS bucketDate,
				aggregates.reading_type AS readingType,
				aggregates.minimum_value AS minimumValue,
				aggregates.maximum_value AS maximumValue,
				aggregates.average_value AS averageValue,
				ranked.reading_value AS latestValue,
				aggregates.reading_count AS readingCount,
				aggregates.latest_recorded_at AS latestRecordedAt
			FROM aggregates
			JOIN ranked ON ranked.bucket_date = aggregates.bucket_date
				AND ranked.reading_type = aggregates.reading_type
				AND ranked.row_number = 1
			ORDER BY aggregates.bucket_date, aggregates.reading_type
			""", nativeQuery = true)
	List<DailyHealthAggregate> summarizeDaily(
			@Param("astronautId") Long astronautId,
			@Param("fromTime") Instant fromTime,
			@Param("toTime") Instant toTime,
			@Param("readingType") String readingType);
}
