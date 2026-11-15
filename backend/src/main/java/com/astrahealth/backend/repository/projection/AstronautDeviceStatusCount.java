package com.astrahealth.backend.repository.projection;

import com.astrahealth.backend.entity.DeviceStatus;

public interface AstronautDeviceStatusCount {
	Long getAstronautId();
	DeviceStatus getDeviceStatus();
	Long getDeviceCount();
}
