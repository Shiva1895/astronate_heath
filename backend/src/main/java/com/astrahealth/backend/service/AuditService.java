package com.astrahealth.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astrahealth.backend.dto.response.AuditEventResponse;
import com.astrahealth.backend.entity.AuditEvent;
import com.astrahealth.backend.repository.AuditEventRepository;
import com.astrahealth.backend.repository.UserAccountRepository;

@Service
@Transactional(readOnly = true)
public class AuditService {

	private final AuditEventRepository auditRepository;
	private final UserAccountRepository userAccountRepository;

	public AuditService(
			AuditEventRepository auditRepository,
			UserAccountRepository userAccountRepository) {
		this.auditRepository = auditRepository;
		this.userAccountRepository = userAccountRepository;
	}

	@Transactional
	public void record(
			Long actorId,
			String action,
			String resourceType,
			Long resourceId,
			String metadata) {
		AuditEvent event = new AuditEvent();
		if (actorId != null) {
			event.setActor(userAccountRepository.getReferenceById(actorId));
		}
		event.setAction(action);
		event.setResourceType(resourceType);
		event.setResourceId(resourceId);
		event.setMetadata(metadata);
		auditRepository.save(event);
	}

	public Page<AuditEventResponse> list(Pageable pageable) {
		return auditRepository.findAllByOrderByOccurredAtDesc(pageable).map(AuditEventResponse::from);
	}
}
