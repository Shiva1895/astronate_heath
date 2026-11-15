package com.astrahealth.backend.service;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.astrahealth.backend.dto.request.DeviceRequest;
import com.astrahealth.backend.dto.response.DeviceHistoryResponse;
import com.astrahealth.backend.dto.response.DeviceResponse;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.Device;
import com.astrahealth.backend.entity.DeviceEventType;
import com.astrahealth.backend.entity.DeviceHistory;
import com.astrahealth.backend.entity.DeviceStatus;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.DeviceHistoryRepository;
import com.astrahealth.backend.repository.DeviceRepository;

@Service
@Transactional(readOnly = true)
public class DeviceService {

	private final DeviceRepository deviceRepository;
	private final DeviceHistoryRepository historyRepository;
	private final AstronautRepository astronautRepository;

	public DeviceService(
			DeviceRepository deviceRepository,
			DeviceHistoryRepository historyRepository,
			AstronautRepository astronautRepository) {
		this.deviceRepository = deviceRepository;
		this.historyRepository = historyRepository;
		this.astronautRepository = astronautRepository;
	}

	@Transactional
	public DeviceResponse register(DeviceRequest request) {
		String serialNumber = request.serialNumber().trim().toUpperCase(Locale.ROOT);
		if (deviceRepository.existsBySerialNumberIgnoreCase(serialNumber)) {
			throw new DuplicateResourceException("Device serial number already exists.");
		}
		Device device = new Device();
		apply(device, request, serialNumber);
		device = deviceRepository.save(device);
		record(device, null, DeviceEventType.REGISTERED);
		return DeviceResponse.from(device);
	}

	@Transactional
	public DeviceResponse update(Long id, DeviceRequest request) {
		Device device = getDevice(id);
		String serialNumber = request.serialNumber().trim().toUpperCase(Locale.ROOT);
		if (!serialNumber.equalsIgnoreCase(device.getSerialNumber())
				&& deviceRepository.existsBySerialNumberIgnoreCase(serialNumber)) {
			throw new DuplicateResourceException("Device serial number already exists.");
		}
		apply(device, request, serialNumber);
		return DeviceResponse.from(deviceRepository.save(device));
	}

	public DeviceResponse get(Long id) {
		return DeviceResponse.from(getDevice(id));
	}

	public Page<DeviceResponse> search(
			String query,
			String type,
			DeviceStatus status,
			Pageable pageable) {
		return deviceRepository.search(
				StringUtils.hasText(query) ? query.trim() : null,
				StringUtils.hasText(type) ? type.trim() : null,
				status,
				pageable).map(DeviceResponse::from);
	}

	public Page<DeviceResponse> getAstronautDevices(Long astronautId, Pageable pageable) {
		if (!astronautRepository.existsById(astronautId)) {
			throw new ResourceNotFoundException("Astronaut not found with ID: " + astronautId);
		}
		return deviceRepository.findAllByAssignedAstronautId(astronautId, pageable).map(DeviceResponse::from);
	}

	@Transactional
	public DeviceResponse assign(Long deviceId, Long astronautId) {
		Device device = getDevice(deviceId);
		if (device.getStatus() == DeviceStatus.DECOMMISSIONED) {
			throw new InvalidRequestException("A decommissioned device cannot be assigned.");
		}
		if (device.getAssignedAstronaut() != null
				&& !device.getAssignedAstronaut().getId().equals(astronautId)) {
			throw new InvalidRequestException("Unassign the device from its current astronaut before reassignment.");
		}
		Astronaut astronaut = astronautRepository.findById(astronautId)
				.orElseThrow(() -> new ResourceNotFoundException("Astronaut not found with ID: " + astronautId));
		if (device.getAssignedAstronaut() == null) {
			device.setAssignedAstronaut(astronaut);
			deviceRepository.save(device);
			record(device, astronaut, DeviceEventType.ASSIGNED);
		}
		return DeviceResponse.from(device);
	}

	@Transactional
	public void unassign(Long deviceId) {
		Device device = getDevice(deviceId);
		Astronaut astronaut = device.getAssignedAstronaut();
		if (astronaut != null) {
			device.setAssignedAstronaut(null);
			deviceRepository.save(device);
			record(device, astronaut, DeviceEventType.UNASSIGNED);
		}
	}

	@Transactional
	public DeviceResponse updateStatus(Long deviceId, DeviceStatus status) {
		Device device = getDevice(deviceId);
		if (device.getStatus() != status) {
			device.setStatus(status);
			deviceRepository.save(device);
			record(device, device.getAssignedAstronaut(), DeviceEventType.STATUS_CHANGED);
		}
		return DeviceResponse.from(device);
	}

	public Page<DeviceHistoryResponse> getHistory(Long deviceId, Pageable pageable) {
		if (!deviceRepository.existsById(deviceId)) {
			throw new ResourceNotFoundException("Device not found with ID: " + deviceId);
		}
		return historyRepository.findAllByDeviceIdOrderByOccurredAtDesc(deviceId, pageable)
				.map(DeviceHistoryResponse::from);
	}

	private Device getDevice(Long id) {
		return deviceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + id));
	}

	private void apply(Device device, DeviceRequest request, String serialNumber) {
		device.setDeviceName(request.deviceName().trim());
		device.setSerialNumber(serialNumber);
		device.setDeviceType(request.deviceType().trim().toUpperCase(Locale.ROOT));
		device.setStatus(request.status());
	}

	private void record(Device device, Astronaut astronaut, DeviceEventType eventType) {
		DeviceHistory history = new DeviceHistory();
		history.setDevice(device);
		history.setAstronaut(astronaut);
		history.setEventType(eventType);
		historyRepository.save(history);
	}
}
