package com.iot.platform.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("audit_log")
public class AuditLog {

    @PrimaryKeyColumn(name = "tenant_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID tenantId;

    @PrimaryKeyColumn(name = "day_bucket", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String dayBucket;

    @PrimaryKeyColumn(name = "logged_at", ordinal = 2, type = PrimaryKeyType.CLUSTERED,
            ordering = org.springframework.data.cassandra.core.cql.Ordering.DESCENDING)
    private Instant loggedAt;

    @Column("user_id")
    private UUID userId;

    @Column("action")
    private String action;

    @Column("resource")
    private String resource;

    @Column("detail")
    private String detail;

    public AuditLog() {}

    public AuditLog(UUID tenantId, String dayBucket, UUID userId,
                    String action, String resource, String detail) {
        this.tenantId = tenantId;
        this.dayBucket = dayBucket;
        this.loggedAt = Instant.now();
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.detail = detail;
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getDayBucket() { return dayBucket; }
    public void setDayBucket(String dayBucket) { this.dayBucket = dayBucket; }
    public Instant getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Instant loggedAt) { this.loggedAt = loggedAt; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
