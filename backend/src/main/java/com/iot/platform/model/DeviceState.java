package com.iot.platform.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("device_state")
public class DeviceState {

    @PrimaryKeyColumn(name = "tenant_id", type = PrimaryKeyType.PARTITIONED)
    private UUID tenantId;

    @PrimaryKeyColumn(name = "device_id", type = PrimaryKeyType.CLUSTERED)
    private UUID deviceId;

    @Column("online")
    private Boolean online;

    @Column("last_seen")
    private Instant lastSeen;

    @Column("ip_address")
    private String ipAddress;

    @Column("rssi")
    private Integer rssi;

    public DeviceState() {}

    public DeviceState(UUID tenantId, UUID deviceId, Boolean online, Instant lastSeen) {
        this.tenantId = tenantId;
        this.deviceId = deviceId;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public Boolean getOnline() { return online; }
    public void setOnline(Boolean online) { this.online = online; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getRssi() { return rssi; }
    public void setRssi(Integer rssi) { this.rssi = rssi; }
}
