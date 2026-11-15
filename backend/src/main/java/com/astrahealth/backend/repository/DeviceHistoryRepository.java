package com.astrahealth.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.astrahealth.backend.entity.DeviceHistory;

public interface DeviceHistoryRepository extends JpaRepository<DeviceHistory, Long> {

	Page<DeviceHistory> findAllByDeviceIdOrderByOccurredAtDesc(Long deviceId, Pageable pageable);
}
