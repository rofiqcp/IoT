package com.iot.platform.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.platform.model.DeviceState;
import com.iot.platform.repository.DeviceStateRepository;
import com.iot.platform.service.AlarmService;
import com.iot.platform.service.TelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/** Consumes telemetry, device-state, and command-ack messages from Kafka. */
@Component
public class KafkaConsumers {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumers.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private final TelemetryService telemetryService;
    private final AlarmService alarmService;
    private final DeviceStateRepository deviceStateRepository;
    private final SimpMessagingTemplate wsTemplate;

    public KafkaConsumers(TelemetryService telemetryService,
                          AlarmService alarmService,
                          DeviceStateRepository deviceStateRepository,
                          SimpMessagingTemplate wsTemplate) {
        this.telemetryService = telemetryService;
        this.alarmService = alarmService;
        this.deviceStateRepository = deviceStateRepository;
        this.wsTemplate = wsTemplate;
    }

    @KafkaListener(topics = "iot.telemetry", groupId = "backend-telemetry-cg")
    public void consumeTelemetry(String message) {
        try {
            JsonNode node = mapper.readTree(message);
            UUID tenantId = UUID.fromString(node.get("tenantId").asText());
            UUID deviceId = UUID.fromString(node.get("deviceId").asText());
            String metric = node.get("metric").asText();
            double value = node.get("value").asDouble();
            String unit = node.has("unit") ? node.get("unit").asText() : "";
            Instant ts = node.has("timestamp")
                    ? Instant.ofEpochMilli(node.get("timestamp").asLong())
                    : Instant.now();

            telemetryService.save(tenantId, deviceId, metric, value, unit, ts);
            alarmService.evaluateTelemetry(tenantId, deviceId, metric, value);

            // Push realtime update via WebSocket
            wsTemplate.convertAndSend(
                    "/topic/telemetry/" + tenantId + "/" + deviceId, message);
        } catch (Exception e) {
            log.error("Error processing telemetry: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "iot.device-state", groupId = "backend-state-cg")
    public void consumeDeviceState(String message) {
        try {
            JsonNode node = mapper.readTree(message);
            UUID tenantId = UUID.fromString(node.get("tenantId").asText());
            UUID deviceId = UUID.fromString(node.get("deviceId").asText());
            boolean online = node.get("online").asBoolean();

            DeviceState state = deviceStateRepository.findByTenantIdAndDeviceId(tenantId, deviceId);
            if (state == null) {
                state = new DeviceState(tenantId, deviceId, online, Instant.now());
            } else {
                state.setOnline(online);
                state.setLastSeen(Instant.now());
            }
            if (node.has("ip")) state.setIpAddress(node.get("ip").asText());
            if (node.has("rssi")) state.setRssi(node.get("rssi").asInt());
            deviceStateRepository.save(state);

            wsTemplate.convertAndSend(
                    "/topic/state/" + tenantId + "/" + deviceId, message);
        } catch (Exception e) {
            log.error("Error processing device state: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "iot.command-acks", groupId = "backend-acks-cg")
    public void consumeCommandAck(String message) {
        try {
            log.info("Command ACK received: {}", message);
            // In production: update command status in Cassandra
            // and notify the frontend via WebSocket
        } catch (Exception e) {
            log.error("Error processing command ack: {}", e.getMessage(), e);
        }
    }
}
