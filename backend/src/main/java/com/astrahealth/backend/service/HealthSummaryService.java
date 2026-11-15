package com.astrahealth.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.response.HealthSummaryBucket;
import com.astrahealth.backend.dto.response.HealthPeriodSummary;
import com.astrahealth.backend.dto.response.HealthSummaryComparison;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import com.astrahealth.backend.repository.projection.DailyHealthAggregate;

@Service
@Transactional(readOnly = true)
public class HealthSummaryService {

	private static final Duration MAX_SUMMARY_RANGE = Duration.ofDays(366 * 5L);

	private final HealthReadingRepository readingRepository;
	private final AstronautRepository astronautRepository;

	public HealthSummaryService(
			HealthReadingRepository readingRepository,
			AstronautRepository astronautRepository) {
		this.readingRepository = readingRepository;
		this.astronautRepository = astronautRepository;
	}

	public List<HealthSummaryBucket> daily(
			Long astronautId,
			Instant fromTime,
			Instant toTime,
			ReadingType readingType) {
		validateRange(fromTime, toTime);
		if (!astronautRepository.existsById(astronautId)) {
			throw new ResourceNotFoundException("Astronaut not found with ID: " + astronautId);
		}
		return readingRepository.summarizeDaily(
						astronautId,
						fromTime,
						toTime,
						readingType == null ? null : readingType.name())
				.stream()
				.map(this::toBucket)
				.toList();
	}

	public List<HealthSummaryBucket> weekly(
			Long astronautId,
			Instant fromTime,
			Instant toTime,
			ReadingType readingType) {
		Map<WeeklyKey, WeeklyAccumulator> weeks = new LinkedHashMap<>();
		for (HealthSummaryBucket dailyBucket : daily(astronautId, fromTime, toTime, readingType)) {
			LocalDate weekStart = dailyBucket.bucketDate()
					.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			weeks.computeIfAbsent(
					new WeeklyKey(weekStart, dailyBucket.readingType()),
					ignored -> new WeeklyAccumulator())
					.add(dailyBucket);
		}
		return weeks.entrySet().stream()
				.map(entry -> entry.getValue().toBucket(entry.getKey()))
				.sorted(Comparator.comparing(HealthSummaryBucket::bucketDate)
						.thenComparing(bucket -> bucket.readingType().name()))
				.toList();
	}

	public HealthSummaryComparison compare(
			Long astronautId,
			ReadingType readingType,
			Instant firstFrom,
			Instant firstTo,
			Instant secondFrom,
			Instant secondTo) {
		if (readingType == null) {
			throw new InvalidRequestException("Reading type is required for period comparison.");
		}
		HealthPeriodSummary first = summarizePeriod(
				daily(astronautId, firstFrom, firstTo, readingType), firstFrom, firstTo);
		HealthPeriodSummary second = summarizePeriod(
				daily(astronautId, secondFrom, secondTo, readingType), secondFrom, secondTo);
		BigDecimal difference = null;
		BigDecimal percentChange = null;
		if (first.averageValue() != null && second.averageValue() != null) {
			difference = second.averageValue().subtract(first.averageValue());
			if (first.averageValue().compareTo(BigDecimal.ZERO) != 0) {
				percentChange = difference
						.multiply(BigDecimal.valueOf(100))
						.divide(first.averageValue().abs(), 4, RoundingMode.HALF_UP);
			}
		}
		return new HealthSummaryComparison(readingType, first, second, difference, percentChange);
	}

	private HealthPeriodSummary summarizePeriod(
			List<HealthSummaryBucket> buckets,
			Instant from,
			Instant to) {
		if (buckets.isEmpty()) {
			return new HealthPeriodSummary(from, to, null, null, null, null, 0);
		}
		BigDecimal minimum = null;
		BigDecimal maximum = null;
		BigDecimal weightedAverage = BigDecimal.ZERO;
		BigDecimal latestValue = null;
		Instant latestRecordedAt = null;
		long count = 0;
		for (HealthSummaryBucket bucket : buckets) {
			minimum = minimum == null ? bucket.minimumValue() : minimum.min(bucket.minimumValue());
			maximum = maximum == null ? bucket.maximumValue() : maximum.max(bucket.maximumValue());
			weightedAverage = weightedAverage.add(
					bucket.averageValue().multiply(BigDecimal.valueOf(bucket.readingCount())));
			count += bucket.readingCount();
			if (latestRecordedAt == null || bucket.latestRecordedAt().isAfter(latestRecordedAt)) {
				latestRecordedAt = bucket.latestRecordedAt();
				latestValue = bucket.latestValue();
			}
		}
		return new HealthPeriodSummary(
				from,
				to,
				minimum,
				maximum,
				weightedAverage.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_UP),
				latestValue,
				count);
	}

	private HealthSummaryBucket toBucket(DailyHealthAggregate aggregate) {
		return new HealthSummaryBucket(
				aggregate.getBucketDate(),
				ReadingType.valueOf(aggregate.getReadingType()),
				aggregate.getMinimumValue(),
				aggregate.getMaximumValue(),
				aggregate.getAverageValue(),
				aggregate.getLatestValue(),
				aggregate.getReadingCount(),
				aggregate.getLatestRecordedAt().toInstant());
	}

	private void validateRange(Instant fromTime, Instant toTime) {
		if (fromTime == null || toTime == null || !fromTime.isBefore(toTime)) {
			throw new InvalidRequestException("A valid start and end timestamp are required.");
		}
		if (Duration.between(fromTime, toTime).compareTo(MAX_SUMMARY_RANGE) > 0) {
			throw new InvalidRequestException("Summary time range cannot exceed five years.");
		}
	}

	private record WeeklyKey(LocalDate weekStart, ReadingType readingType) {
	}

	private static final class WeeklyAccumulator {
		private BigDecimal minimum;
		private BigDecimal maximum;
		private BigDecimal weightedAverage = BigDecimal.ZERO;
		private long count;
		private BigDecimal latestValue;
		private Instant latestAt;

		void add(HealthSummaryBucket bucket) {
			minimum = minimum == null ? bucket.minimumValue() : minimum.min(bucket.minimumValue());
			maximum = maximum == null ? bucket.maximumValue() : maximum.max(bucket.maximumValue());
			weightedAverage = weightedAverage.add(
					bucket.averageValue().multiply(BigDecimal.valueOf(bucket.readingCount())));
			count += bucket.readingCount();
			if (latestAt == null || bucket.latestRecordedAt().isAfter(latestAt)) {
				latestAt = bucket.latestRecordedAt();
				latestValue = bucket.latestValue();
			}
		}

		HealthSummaryBucket toBucket(WeeklyKey key) {
			return new HealthSummaryBucket(
					key.weekStart(),
					key.readingType(),
					minimum,
					maximum,
					weightedAverage.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_UP),
					latestValue,
					count,
					latestAt);
		}
	}
}
