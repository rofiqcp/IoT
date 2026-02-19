package com.iot.platform.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("alarms")
public class Alarm {

    @PrimaryKeyColumn(name = "tenant_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID tenantId;

    @PrimaryKeyColumn(name = "triggered_at", ordinal = 1, type = PrimaryKeyType.CLUSTERED,
            ordering = org.springframework.data.cassandra.core.cql.Ordering.DESCENDING)
    private Instant triggeredAt;

    @PrimaryKeyColumn(name = "alarm_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID alarmId;

    @Column("device_id")
    private UUID deviceId;

    @Column("rule_name")
    private String ruleName;

    @Column("severity")
    private String severity;

    @Column("message")
    private String message;

    @Column("active")
    private Boolean active;

    @Column("acknowledged_at")
    private Instant acknowledgedAt;

    @Column("acknowledged_by")
    private UUID acknowledgedBy;

    public Alarm() {}

    public Alarm(UUID tenantId, UUID alarmId, UUID deviceId, String ruleName,
                 String severity, String message) {
        this.tenantId = tenantId;
        this.alarmId = alarmId;
        this.deviceId = deviceId;
        this.ruleName = ruleName;
        this.severity = severity;
        this.message = message;
        this.active = true;
        this.triggeredAt = Instant.now();
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }
    public UUID getAlarmId() { return alarmId; }
    public void setAlarmId(UUID alarmId) { this.alarmId = alarmId; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public UUID getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(UUID acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
}
