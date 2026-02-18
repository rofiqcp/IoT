package com.iot.platform.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("alarm_rules")
public class AlarmRule {

    @PrimaryKeyColumn(name = "tenant_id", type = PrimaryKeyType.PARTITIONED)
    private UUID tenantId;

    @PrimaryKeyColumn(name = "rule_id", type = PrimaryKeyType.CLUSTERED)
    private UUID ruleId;

    @Column("metric_name")
    private String metricName;

    @Column("operator")
    private String operator;  // GT, LT, EQ, GTE, LTE

    @Column("threshold")
    private Double threshold;

    @Column("severity")
    private String severity;

    @Column("enabled")
    private Boolean enabled;

    @Column("created_at")
    private Instant createdAt;

    public AlarmRule() {}

    public AlarmRule(UUID tenantId, UUID ruleId, String metricName, String operator,
                     Double threshold, String severity) {
        this.tenantId = tenantId;
        this.ruleId = ruleId;
        this.metricName = metricName;
        this.operator = operator;
        this.threshold = threshold;
        this.severity = severity;
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
