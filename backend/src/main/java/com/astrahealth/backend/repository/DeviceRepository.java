package com.astrahealth.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.astrahealth.backend.entity.Device;
import com.astrahealth.backend.entity.DeviceStatus;
import com.astrahealth.backend.repository.projection.AstronautDeviceStatusCount;

public interface DeviceRepository extends JpaRepository<Device, Long> {

	boolean existsBySerialNumberIgnoreCase(String serialNumber);

	Optional<Device> findByIdAndSerialNumber(Long id, String serialNumber);

	@Query("""
			select device from Device device
			where (:query is null or lower(device.serialNumber) like lower(concat('%', :query, '%')))
				and (:type is null or lower(device.deviceType) = lower(:type))
				and (:status is null or device.status = :status)
			""")
	Page<Device> search(
			@Param("query") String query,
			@Param("type") String type,
			@Param("status") DeviceStatus status,
			Pageable pageable);

	Page<Device> findAllByAssignedAstronautId(Long astronautId, Pageable pageable);

	@Query("""
			select device.assignedAstronaut.id as astronautId,
				device.status as deviceStatus, count(device.id) as deviceCount
			from Device device
			where device.assignedAstronaut.id in :astronautIds
			group by device.assignedAstronaut.id, device.status
			""")
	java.util.List<AstronautDeviceStatusCount> countGroupedByAstronautAndStatus(
			@Param("astronautIds") java.util.List<Long> astronautIds);
}
