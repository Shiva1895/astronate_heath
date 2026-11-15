package com.astrahealth.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AnomalyRule;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.repository.AnomalyRuleRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTests {

	@Mock
	private AnomalyRuleRepository ruleRepository;
	@Mock
	private HealthReadingRepository readingRepository;
	@Mock
	private AlertService alertService;

	@InjectMocks
	private AnomalyDetectionService anomalyDetectionService;

	@Test
	void configuredMaximumCreatesExplainableAlert() {
		HealthReading reading = reading("150");
		AnomalyRule rule = rule();
		rule.setMaximumValue(new BigDecimal("100"));
		when(ruleRepository.findAllByReadingTypeAndEnabledTrue(ReadingType.HEART_RATE))
				.thenReturn(List.of(rule));

		anomalyDetectionService.analyze(reading);

		verify(alertService).createRuleAlert(
				eq(reading),
				eq(rule),
				contains("above configured maximum"));
	}

	@Test
	void relativeChangeUsesRecentBoundedBaseline() {
		HealthReading reading = reading("100");
		AnomalyRule rule = rule();
		rule.setChangeThresholdValue(new BigDecimal("25"));
		rule.setLookbackCount(2);
		when(ruleRepository.findAllByReadingTypeAndEnabledTrue(ReadingType.HEART_RATE))
				.thenReturn(List.of(rule));
		when(readingRepository
				.findTop100ByAstronautIdAndReadingTypeAndRecordedAtLessThanOrderByRecordedAtDescIdDesc(
						eq(7L), eq(ReadingType.HEART_RATE), any(), any(Pageable.class)))
				.thenReturn(List.of(reading("60"), reading("70")));

		anomalyDetectionService.analyze(reading);

		verify(alertService).createRuleAlert(eq(reading), eq(rule), contains("recent baseline"));
	}

	@Test
	void disabledOrUnconfiguredRulesDoNotCreateAlerts() {
		HealthReading reading = reading("150");
		when(ruleRepository.findAllByReadingTypeAndEnabledTrue(ReadingType.HEART_RATE))
				.thenReturn(List.of());

		anomalyDetectionService.analyze(reading);

		verify(alertService, never()).createRuleAlert(any(), any(), any());
	}

	private AnomalyRule rule() {
		AnomalyRule rule = new AnomalyRule();
		rule.setReadingType(ReadingType.HEART_RATE);
		rule.setPriority(AlertPriority.HIGH);
		rule.setDescription("Configured test rule");
		rule.setEnabled(true);
		return rule;
	}

	private HealthReading reading(String value) {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		HealthReading reading = new HealthReading();
		reading.setAstronaut(astronaut);
		reading.setReadingType(ReadingType.HEART_RATE);
		reading.setValue(new BigDecimal(value));
		reading.setUnit("bpm");
		reading.setRecordedAt(java.time.Instant.parse("2026-11-15T10:00:00Z"));
		return reading;
	}
}
