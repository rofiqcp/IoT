package com.iot.platform.security;

import java.util.UUID;

/** Holds current tenant context extracted from JWT for the request scope. */
public class TenantContext {

    private final UUID tenantId;
    private final UUID userId;
    private final String role;

    public TenantContext(UUID tenantId, UUID userId, String role) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.role = role;
    }

    public UUID getTenantId() { return tenantId; }
    public UUID getUserId() { return userId; }
    public String getRole() { return role; }
}
