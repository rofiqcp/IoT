package com.iot.platform.controller;

import com.iot.platform.model.Command;
import com.iot.platform.security.TenantContext;
import com.iot.platform.service.CommandService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/commands")
public class CommandController {

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','TENANT_ADMIN','OPERATOR')")
    public Command sendCommand(@RequestBody Map<String, String> body, Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return commandService.sendCommand(
                ctx.getTenantId(),
                UUID.fromString(body.get("deviceId")),
                body.get("commandType"),
                body.getOrDefault("payload", "{}"),
                ctx.getUserId()
        );
    }

    @GetMapping("/{deviceId}")
    public List<Command> getCommands(@PathVariable UUID deviceId,
                                     @RequestParam(defaultValue = "50") int limit,
                                     Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return commandService.getCommands(ctx.getTenantId(), deviceId, limit);
    }
}
