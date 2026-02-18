package com.iot.platform.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("commands")
public class Command {

    @PrimaryKeyColumn(name = "tenant_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID tenantId;

    @PrimaryKeyColumn(name = "device_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private UUID deviceId;

    @PrimaryKeyColumn(name = "created_at", ordinal = 2, type = PrimaryKeyType.CLUSTERED,
            ordering = org.springframework.data.cassandra.core.cql.Ordering.DESCENDING)
    private Instant createdAt;

    @PrimaryKeyColumn(name = "command_id", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private UUID commandId;

    @Column("command_type")
    private String commandType;

    @Column("payload")
    private String payload;

    @Column("status")
    private String status;  // PENDING, SENT, ACKED, FAILED, TIMEOUT

    @Column("acked_at")
    private Instant ackedAt;

    @Column("created_by")
    private UUID createdBy;

    public Command() {}

    public Command(UUID tenantId, UUID deviceId, UUID commandId, String commandType,
                   String payload, UUID createdBy) {
        this.tenantId = tenantId;
        this.deviceId = deviceId;
        this.commandId = commandId;
        this.commandType = commandType;
        this.payload = payload;
        this.status = "PENDING";
        this.createdAt = Instant.now();
        this.createdBy = createdBy;
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public UUID getCommandId() { return commandId; }
    public void setCommandId(UUID commandId) { this.commandId = commandId; }
    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getAckedAt() { return ackedAt; }
    public void setAckedAt(Instant ackedAt) { this.ackedAt = ackedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
