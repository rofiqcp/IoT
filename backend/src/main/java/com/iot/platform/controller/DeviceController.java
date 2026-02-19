package com.iot.platform.controller;

import com.iot.platform.model.Device;
import com.iot.platform.model.DeviceState;
import com.iot.platform.repository.DeviceStateRepository;
import com.iot.platform.security.TenantContext;
import com.iot.platform.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceStateRepository deviceStateRepository;

    public DeviceController(DeviceService deviceService,
                            DeviceStateRepository deviceStateRepository) {
        this.deviceService = deviceService;
        this.deviceStateRepository = deviceStateRepository;
    }

    @GetMapping
    public List<Device> listDevices(@RequestParam UUID siteId, Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return deviceService.getDevicesBySite(ctx.getTenantId(), siteId);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDevice(@PathVariable UUID deviceId,
                                            @RequestParam UUID siteId,
                                            Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        Device device = deviceService.getDevice(ctx.getTenantId(), siteId, deviceId);
        return device != null ? ResponseEntity.ok(device) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Device createDevice(@RequestBody Map<String, String> body, Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return deviceService.createDevice(
                ctx.getTenantId(),
                UUID.fromString(body.get("siteId")),
                body.get("name"),
                body.getOrDefault("deviceType", "ESP32")
        );
    }

    @GetMapping("/{deviceId}/state")
    public ResponseEntity<DeviceState> getDeviceState(@PathVariable UUID deviceId,
                                                       Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        DeviceState state = deviceStateRepository.findByTenantIdAndDeviceId(
                ctx.getTenantId(), deviceId);
        return state != null ? ResponseEntity.ok(state) : ResponseEntity.notFound().build();
    }

    @GetMapping("/states")
    public List<DeviceState> listDeviceStates(Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return deviceStateRepository.findByTenantId(ctx.getTenantId());
    }
}
