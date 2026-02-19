package com.iot.platform.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("sites")
public class Site {

    @PrimaryKeyColumn(name = "tenant_id", type = PrimaryKeyType.PARTITIONED)
    private UUID tenantId;

    @PrimaryKeyColumn(name = "site_id", type = PrimaryKeyType.CLUSTERED)
    private UUID siteId;

    @Column("name")
    private String name;

    @Column("location")
    private String location;

    @Column("created_at")
    private Instant createdAt;

    public Site() {}

    public Site(UUID tenantId, UUID siteId, String name, String location) {
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.name = name;
        this.location = location;
        this.createdAt = Instant.now();
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getSiteId() { return siteId; }
    public void setSiteId(UUID siteId) { this.siteId = siteId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
