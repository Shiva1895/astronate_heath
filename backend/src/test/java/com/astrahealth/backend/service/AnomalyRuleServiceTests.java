package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.astrahealth.backend.dto.request.AnomalyRuleRequest;
import com.astrahealth.backend.entity.AlertPriority;
import com.astrahealth.backend.entity.AnomalyRule;
import com.astrahealth.backend.entity.ReadingType;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.repository.AnomalyRuleRepository;

@ExtendWith(MockitoExtension.class)
class AnomalyRuleServiceTests {

	@Mock
	private AnomalyRuleRepository ruleRepository;

	@InjectMocks
	private AnomalyRuleService ruleService;

	@Test
	void administratorCanPersistExplicitlyConfiguredRule() {
		when(ruleRepository.save(any(AnomalyRule.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var response = ruleService.create(new AnomalyRuleRequest(
				ReadingType.HEART_RATE,
				null,
				new BigDecimal("999"),
				null,
				5,
				AlertPriority.INFORMATIONAL,
				"Demonstration rule; replace with organization policy.",
				false));

		assertEquals(ReadingType.HEART_RATE, response.readingType());
		assertEquals(5, response.lookbackCount());
		assertEquals(false, response.enabled());
		verify(ruleRepository).save(any(AnomalyRule.class));
	}

	@Test
	void ruleWithoutThresholdsIsRejected() {
		assertThrows(
				InvalidRequestException.class,
				() -> ruleService.create(new AnomalyRuleRequest(
						ReadingType.HEART_RATE,
						null,
						null,
						null,
						null,
						AlertPriority.HIGH,
						"Invalid rule",
						true)));

		verify(ruleRepository, never()).save(any(AnomalyRule.class));
	}
}
