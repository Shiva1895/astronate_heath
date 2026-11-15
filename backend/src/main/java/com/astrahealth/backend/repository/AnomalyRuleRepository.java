package com.astrahealth.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.AnomalyRule;
import com.astrahealth.backend.entity.ReadingType;

public interface AnomalyRuleRepository extends JpaRepository<AnomalyRule, Long> {
	List<AnomalyRule> findAllByReadingTypeAndEnabledTrue(ReadingType readingType);
}
