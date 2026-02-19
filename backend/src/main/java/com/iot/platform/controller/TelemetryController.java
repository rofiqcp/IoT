package com.iot.platform.controller;

import com.iot.platform.model.TelemetryRecord;
import com.iot.platform.security.TenantContext;
import com.iot.platform.service.TelemetryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping("/{deviceId}")
    public List<TelemetryRecord> getTelemetry(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "100") int limit,
            Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        if (date != null) {
            return telemetryService.getByDeviceAndDate(ctx.getTenantId(), deviceId, date, limit);
        }
        return telemetryService.getLatest(ctx.getTenantId(), deviceId, limit);
    }
}
