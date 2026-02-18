package com.iot.platform.service;

import com.iot.platform.model.Alarm;
import com.iot.platform.model.AlarmRule;
import com.iot.platform.repository.AlarmRepository;
import com.iot.platform.repository.AlarmRuleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final AlarmRuleRepository alarmRuleRepository;

    public AlarmService(AlarmRepository alarmRepository, AlarmRuleRepository alarmRuleRepository) {
        this.alarmRepository = alarmRepository;
        this.alarmRuleRepository = alarmRuleRepository;
    }

    /** Evaluate telemetry value against all active alarm rules for the tenant. */
    public void evaluateTelemetry(UUID tenantId, UUID deviceId, String metricName, double value) {
        List<AlarmRule> rules = alarmRuleRepository.findByTenantId(tenantId);
        for (AlarmRule rule : rules) {
            if (!rule.getEnabled() || !rule.getMetricName().equals(metricName)) {
                continue;
            }
            boolean triggered = switch (rule.getOperator()) {
                case "GT"  -> value > rule.getThreshold();
                case "GTE" -> value >= rule.getThreshold();
                case "LT"  -> value < rule.getThreshold();
                case "LTE" -> value <= rule.getThreshold();
                case "EQ"  -> value == rule.getThreshold();
                default    -> false;
            };
            if (triggered) {
                Alarm alarm = new Alarm(tenantId, UUID.randomUUID(), deviceId, rule.getMetricName(),
                        rule.getSeverity(), metricName + " " + rule.getOperator() + " " + rule.getThreshold());
                alarmRepository.save(alarm);
            }
        }
    }

    public List<Alarm> getAlarms(UUID tenantId, int limit) {
        return alarmRepository.findByTenantId(tenantId, limit);
    }

    public Alarm acknowledgeAlarm(UUID tenantId, UUID alarmId, UUID userId) {
        // Find and update — simplified for Cassandra's data model
        List<Alarm> alarms = alarmRepository.findByTenantId(tenantId, 1000);
        for (Alarm alarm : alarms) {
            if (alarm.getAlarmId().equals(alarmId)) {
                alarm.setActive(false);
                alarm.setAcknowledgedAt(Instant.now());
                alarm.setAcknowledgedBy(userId);
                return alarmRepository.save(alarm);
            }
        }
        throw new RuntimeException("Alarm not found");
    }

    // Alarm rules CRUD
    public List<AlarmRule> getRules(UUID tenantId) {
        return alarmRuleRepository.findByTenantId(tenantId);
    }

    public AlarmRule createRule(UUID tenantId, String metricName, String operator,
                                Double threshold, String severity) {
        AlarmRule rule = new AlarmRule(tenantId, UUID.randomUUID(), metricName, operator, threshold, severity);
        return alarmRuleRepository.save(rule);
    }
}
