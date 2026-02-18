package com.iot.platform.service;

import com.iot.platform.model.Device;
import com.iot.platform.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getDevicesBySite(UUID tenantId, UUID siteId) {
        return deviceRepository.findByTenantIdAndSiteId(tenantId, siteId);
    }

    public Device getDevice(UUID tenantId, UUID siteId, UUID deviceId) {
        return deviceRepository.findByTenantIdAndSiteIdAndDeviceId(tenantId, siteId, deviceId);
    }

    public Device createDevice(UUID tenantId, UUID siteId, String name, String deviceType) {
        UUID deviceId = UUID.randomUUID();
        Device device = new Device(tenantId, siteId, deviceId, name, deviceType);
        device.setCredentialKey(UUID.randomUUID().toString().replace("-", ""));
        return deviceRepository.save(device);
    }

    public void deleteDevice(UUID tenantId, UUID siteId, UUID deviceId) {
        Device device = deviceRepository.findByTenantIdAndSiteIdAndDeviceId(tenantId, siteId, deviceId);
        if (device != null) {
            deviceRepository.delete(device);
        }
    }
}
