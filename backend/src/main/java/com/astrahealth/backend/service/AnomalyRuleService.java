package com.astrahealth.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.request.AnomalyRuleRequest;
import com.astrahealth.backend.dto.response.AnomalyRuleResponse;
import com.astrahealth.backend.entity.AnomalyRule;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AnomalyRuleRepository;

@Service
@Transactional(readOnly = true)
public class AnomalyRuleService {

	private final AnomalyRuleRepository ruleRepository;

	public AnomalyRuleService(AnomalyRuleRepository ruleRepository) {
		this.ruleRepository = ruleRepository;
	}

	public List<AnomalyRuleResponse> list() {
		return ruleRepository.findAll().stream().map(AnomalyRuleResponse::from).toList();
	}

	@Transactional
	public AnomalyRuleResponse create(AnomalyRuleRequest request) {
		AnomalyRule rule = new AnomalyRule();
		apply(rule, request);
		return AnomalyRuleResponse.from(ruleRepository.save(rule));
	}

	@Transactional
	public AnomalyRuleResponse update(Long id, AnomalyRuleRequest request) {
		AnomalyRule rule = ruleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Anomaly rule not found with ID: " + id));
		apply(rule, request);
		return AnomalyRuleResponse.from(ruleRepository.save(rule));
	}

	private void apply(AnomalyRule rule, AnomalyRuleRequest request) {
		if (request == null || request.readingType() == null || request.priority() == null
				|| request.description() == null || request.description().isBlank()) {
			throw new InvalidRequestException("Anomaly rule fields are invalid.");
		}
		if (request.minimumValue() == null && request.maximumValue() == null
				&& request.changeThresholdValue() == null) {
			throw new InvalidRequestException("At least one anomaly threshold is required.");
		}
		if (request.minimumValue() != null && request.maximumValue() != null
				&& request.minimumValue().compareTo(request.maximumValue()) >= 0) {
			throw new InvalidRequestException("Minimum threshold must be below maximum threshold.");
		}
		int lookbackCount = request.lookbackCount() == null ? 5 : request.lookbackCount();
		if (lookbackCount < 1 || lookbackCount > 100) {
			throw new InvalidRequestException("Lookback count must be between 1 and 100.");
		}
		rule.setReadingType(request.readingType());
		rule.setMinimumValue(request.minimumValue());
		rule.setMaximumValue(request.maximumValue());
		rule.setChangeThresholdValue(request.changeThresholdValue());
		rule.setLookbackCount(lookbackCount);
		rule.setPriority(request.priority());
		rule.setDescription(request.description().trim());
		rule.setEnabled(request.enabled());
	}
}
