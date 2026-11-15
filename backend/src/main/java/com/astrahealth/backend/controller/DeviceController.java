package com.astrahealth.backend.controller;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astrahealth.backend.dto.request.AssignDeviceRequest;
import com.astrahealth.backend.dto.request.DeviceRequest;
import com.astrahealth.backend.dto.request.DeviceStatusRequest;
import com.astrahealth.backend.dto.response.DeviceHistoryResponse;
import com.astrahealth.backend.dto.response.DeviceResponse;
import com.astrahealth.backend.entity.DeviceStatus;
import com.astrahealth.backend.exception.InvalidRequestException;
import com.astrahealth.backend.service.DeviceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

	private static final Set<String> SORT_FIELDS =
			Set.of("id", "deviceName", "serialNumber", "deviceType", "status", "registeredAt");
	private final DeviceService deviceService;

	public DeviceController(DeviceService deviceService) {
		this.deviceService = deviceService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public DeviceResponse register(@Valid @RequestBody DeviceRequest request) {
		return deviceService.register(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public DeviceResponse update(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
		return deviceService.update(id, request);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MISSION_CONTROL')")
	public DeviceResponse get(@PathVariable Long id) {
		return deviceService.get(id);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'MISSION_CONTROL')")
	public Page<DeviceResponse> search(
			@RequestParam(required = false) String query,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) DeviceStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "serialNumber") String sortBy,
			@RequestParam(defaultValue = "ASC") String direction) {
		if (page < 0 || size < 1 || size > 100 || !SORT_FIELDS.contains(sortBy)) {
			throw new InvalidRequestException("Device pagination or sort parameter is invalid.");
		}
		Sort.Direction sortDirection;
		try {
			sortDirection = Sort.Direction.fromString(direction);
		} catch (IllegalArgumentException exception) {
			throw new InvalidRequestException("Sort direction must be ASC or DESC.");
		}
		return deviceService.search(
				query,
				type,
				status,
				PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
	}

	@PutMapping("/{id}/assign")
	@PreAuthorize("hasRole('ADMIN')")
	public DeviceResponse assign(@PathVariable Long id, @Valid @RequestBody AssignDeviceRequest request) {
		return deviceService.assign(id, request.astronautId());
	}

	@PutMapping("/{id}/unassign")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> unassign(@PathVariable Long id) {
		deviceService.unassign(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public DeviceResponse updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody DeviceStatusRequest request) {
		return deviceService.updateStatus(id, request.status());
	}

	@GetMapping("/{id}/history")
	@PreAuthorize("hasAnyRole('ADMIN', 'MISSION_CONTROL')")
	public Page<DeviceHistoryResponse> getHistory(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
		return deviceService.getHistory(
				id,
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt")));
	}

	@GetMapping("/astronauts/{astronautId}")
	@PreAuthorize("hasRole('MISSION_CONTROL') or @healthAccessService.canAccessAstronaut(#p0, authentication)")
	public Page<DeviceResponse> getAstronautDevices(
			@PathVariable Long astronautId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100.");
		}
		return deviceService.getAstronautDevices(
				astronautId,
				PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "serialNumber")));
	}
}
