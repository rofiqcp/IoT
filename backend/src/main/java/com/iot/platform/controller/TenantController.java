package com.iot.platform.controller;

import com.iot.platform.model.Tenant;
import com.iot.platform.repository.TenantRepository;
import com.iot.platform.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantRepository tenantRepository;

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<Tenant> listTenants() {
        return tenantRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Tenant createTenant(@RequestBody Map<String, String> body) {
        Tenant tenant = new Tenant(UUID.randomUUID(), body.get("name"));
        return tenantRepository.save(tenant);
    }

    @GetMapping("/me")
    public ResponseEntity<?> myTenant(Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return tenantRepository.findById(ctx.getTenantId())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
