package com.iot.platform.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("devices")
public class Device {

    @PrimaryKeyColumn(name = "tenant_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID tenantId;

    @PrimaryKeyColumn(name = "site_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private UUID siteId;

    @PrimaryKeyColumn(name = "device_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID deviceId;

    @Column("area_id")
    private UUID areaId;

    @Column("line_id")
    private UUID lineId;

    @Column("name")
    private String name;

    @Column("device_type")
    private String deviceType;

    @Column("credential_key")
    private String credentialKey;

    @Column("firmware_version")
    private String firmwareVersion;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    public Device() {}

    public Device(UUID tenantId, UUID siteId, UUID deviceId, String name, String deviceType) {
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.deviceId = deviceId;
        this.name = name;
        this.deviceType = deviceType;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and setters
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getSiteId() { return siteId; }
    public void setSiteId(UUID siteId) { this.siteId = siteId; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public UUID getAreaId() { return areaId; }
    public void setAreaId(UUID areaId) { this.areaId = areaId; }
    public UUID getLineId() { return lineId; }
    public void setLineId(UUID lineId) { this.lineId = lineId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getCredentialKey() { return credentialKey; }
    public void setCredentialKey(String credentialKey) { this.credentialKey = credentialKey; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
