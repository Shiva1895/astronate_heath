package com.astrahealth.backend.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.entity.AnomalyRule;
import com.astrahealth.backend.entity.HealthReading;
import com.astrahealth.backend.repository.AnomalyRuleRepository;
import com.astrahealth.backend.repository.HealthReadingRepository;

@Service
@Transactional(readOnly = true)
public class AnomalyDetectionService {

	private final AnomalyRuleRepository ruleRepository;
	private final HealthReadingRepository readingRepository;
	private final AlertService alertService;

	public AnomalyDetectionService(
			AnomalyRuleRepository ruleRepository,
			HealthReadingRepository readingRepository,
			AlertService alertService) {
		this.ruleRepository = ruleRepository;
		this.readingRepository = readingRepository;
		this.alertService = alertService;
	}

	@Transactional
	public void analyze(HealthReading reading) {
		for (AnomalyRule rule : ruleRepository.findAllByReadingTypeAndEnabledTrue(reading.getReadingType())) {
			List<String> reasons = evaluate(reading, rule);
			if (!reasons.isEmpty()) {
				alertService.createRuleAlert(reading, rule, String.join(" ", reasons));
			}
		}
	}

	private List<String> evaluate(HealthReading reading, AnomalyRule rule) {
		List<String> reasons = new ArrayList<>();
		BigDecimal value = reading.getValue();
		if (rule.getMinimumValue() != null && value.compareTo(rule.getMinimumValue()) < 0) {
			reasons.add("Value " + value.toPlainString() + " " + reading.getUnit()
					+ " is below configured minimum " + rule.getMinimumValue().toPlainString() + ".");
		}
		if (rule.getMaximumValue() != null && value.compareTo(rule.getMaximumValue()) > 0) {
			reasons.add("Value " + value.toPlainString() + " " + reading.getUnit()
					+ " is above configured maximum " + rule.getMaximumValue().toPlainString() + ".");
		}
		if (rule.getChangeThresholdValue() != null) {
			List<HealthReading> recent = readingRepository
					.findTop100ByAstronautIdAndReadingTypeAndRecordedAtLessThanOrderByRecordedAtDescIdDesc(
							reading.getAstronaut().getId(),
							reading.getReadingType(),
							reading.getRecordedAt(),
							PageRequest.of(0, rule.getLookbackCount()));
			if (!recent.isEmpty()) {
				BigDecimal average = recent.stream()
						.map(HealthReading::getValue)
						.reduce(BigDecimal.ZERO, BigDecimal::add)
						.divide(BigDecimal.valueOf(recent.size()), 6, java.math.RoundingMode.HALF_UP);
				BigDecimal difference = value.subtract(average).abs();
				if (difference.compareTo(rule.getChangeThresholdValue()) >= 0) {
					reasons.add("Value changed by " + difference.toPlainString() + " " + reading.getUnit()
							+ " from the recent baseline, meeting the configured change threshold of "
							+ rule.getChangeThresholdValue().toPlainString() + ".");
				}
			}
		}
		return reasons;
	}
}
