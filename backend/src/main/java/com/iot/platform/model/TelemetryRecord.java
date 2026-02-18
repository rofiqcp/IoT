package com.iot.platform.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

/** Time-series telemetry data partitioned by device + day bucket. */
@Table("telemetry_by_device")
public class TelemetryRecord {

    @PrimaryKeyColumn(name = "tenant_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID tenantId;

    @PrimaryKeyColumn(name = "device_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private UUID deviceId;

    @PrimaryKeyColumn(name = "day_bucket", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
    private String dayBucket;

    @PrimaryKeyColumn(name = "recorded_at", ordinal = 3, type = PrimaryKeyType.CLUSTERED,
            ordering = org.springframework.data.cassandra.core.cql.Ordering.DESCENDING)
    private Instant recordedAt;

    @PrimaryKeyColumn(name = "metric_name", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    private String metricName;

    @Column("metric_value")
    private Double metricValue;

    @Column("unit")
    private String unit;

    public TelemetryRecord() {}

    public TelemetryRecord(UUID tenantId, UUID deviceId, String dayBucket,
                           Instant recordedAt, String metricName,
                           Double metricValue, String unit) {
        this.tenantId = tenantId;
        this.deviceId = deviceId;
        this.dayBucket = dayBucket;
        this.recordedAt = recordedAt;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.unit = unit;
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public String getDayBucket() { return dayBucket; }
    public void setDayBucket(String dayBucket) { this.dayBucket = dayBucket; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public Double getMetricValue() { return metricValue; }
    public void setMetricValue(Double metricValue) { this.metricValue = metricValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
