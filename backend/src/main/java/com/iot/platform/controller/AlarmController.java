package com.iot.platform.controller;

import com.iot.platform.model.Alarm;
import com.iot.platform.model.AlarmRule;
import com.iot.platform.security.TenantContext;
import com.iot.platform.service.AlarmService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @GetMapping
    public List<Alarm> listAlarms(@RequestParam(defaultValue = "100") int limit,
                                  Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return alarmService.getAlarms(ctx.getTenantId(), limit);
    }

    @PostMapping("/{alarmId}/acknowledge")
    @PreAuthorize("hasAnyRole('SUPERADMIN','TENANT_ADMIN','OPERATOR')")
    public Alarm acknowledgeAlarm(@PathVariable UUID alarmId, Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return alarmService.acknowledgeAlarm(ctx.getTenantId(), alarmId, ctx.getUserId());
    }

    // --- Alarm Rules ---

    @GetMapping("/rules")
    public List<AlarmRule> listRules(Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return alarmService.getRules(ctx.getTenantId());
    }

    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('SUPERADMIN','TENANT_ADMIN')")
    public AlarmRule createRule(@RequestBody Map<String, String> body, Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return alarmService.createRule(
                ctx.getTenantId(),
                body.get("metricName"),
                body.get("operator"),
                Double.parseDouble(body.get("threshold")),
                body.getOrDefault("severity", "WARNING")
        );
    }
}
