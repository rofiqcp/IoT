package com.iot.platform.model;

import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("tenants")
public class Tenant {

    @PrimaryKey("tenant_id")
    private UUID tenantId;

    @Column("name")
    private String name;

    @Column("created_at")
    private Instant createdAt;

    public Tenant() {}

    public Tenant(UUID tenantId, String name) {
        this.tenantId = tenantId;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
