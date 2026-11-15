package com.astrahealth.backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.astrahealth.backend.dto.request.DeviceRequest;
import com.astrahealth.backend.entity.Device;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.entity.DeviceEventType;
import com.astrahealth.backend.entity.DeviceHistory;
import com.astrahealth.backend.entity.DeviceStatus;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.repository.AstronautRepository;
import com.astrahealth.backend.repository.DeviceHistoryRepository;
import com.astrahealth.backend.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTests {

	@Mock
	private DeviceRepository deviceRepository;

	@Mock
	private DeviceHistoryRepository historyRepository;

	@Mock
	private AstronautRepository astronautRepository;

	@InjectMocks
	private DeviceService deviceService;

	@Test
	void duplicateSerialNumberIsRejected() {
		when(deviceRepository.existsBySerialNumberIgnoreCase("SERIAL-1")).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> deviceService.register(request()));
		verify(deviceRepository, never()).save(any(Device.class));
		verify(historyRepository, never()).save(any(DeviceHistory.class));
	}

	@Test
	void registrationRecordsDeviceHistory() {
		when(deviceRepository.existsBySerialNumberIgnoreCase("SERIAL-1")).thenReturn(false);
		when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
			Device device = invocation.getArgument(0);
			device.setStatus(DeviceStatus.ACTIVE);
			return device;
		});

		deviceService.register(request());

		verify(historyRepository).save(any(DeviceHistory.class));
	}

	@Test
	void assignmentToMissingAstronautIsRejectedWithoutSaving() {
		Device device = new Device();
		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
		when(astronautRepository.findById(7L)).thenReturn(Optional.empty());

		org.junit.jupiter.api.Assertions.assertThrows(
				com.astrahealth.backend.exception.ResourceNotFoundException.class,
				() -> deviceService.assign(1L, 7L));
		verify(deviceRepository, never()).save(device);
		verify(historyRepository, never()).save(any(DeviceHistory.class));
	}

	@Test
	void unassignmentRecordsHistoryAndPreservesTheDevice() {
		Device device = new Device();
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		device.setAssignedAstronaut(astronaut);
		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
		when(deviceRepository.save(device)).thenReturn(device);
		when(historyRepository.save(any(DeviceHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

		deviceService.unassign(1L);

		assertEquals(null, device.getAssignedAstronaut());
		org.mockito.ArgumentCaptor<DeviceHistory> captor = org.mockito.ArgumentCaptor.forClass(DeviceHistory.class);
		verify(historyRepository).save(captor.capture());
		assertEquals(DeviceEventType.UNASSIGNED, captor.getValue().getEventType());
		assertEquals(7L, captor.getValue().getAstronaut().getId());
	}

	private DeviceRequest request() {
		return new DeviceRequest("Wearable", "SERIAL-1", "HEART_RATE", DeviceStatus.ACTIVE);
	}
}
