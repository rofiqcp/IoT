package com.iot.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iot.platform.model.Command;
import com.iot.platform.repository.CommandRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommandService {

    private final CommandRepository commandRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommandService(CommandRepository commandRepository,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.commandRepository = commandRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Command sendCommand(UUID tenantId, UUID deviceId, String commandType,
                                String payload, UUID userId) {
        UUID commandId = UUID.randomUUID();
        Command cmd = new Command(tenantId, deviceId, commandId, commandType, payload, userId);
        commandRepository.save(cmd);

        // Publish to Kafka for the bridge to forward via MQTT
        try {
            String kafkaKey = tenantId + "|" + deviceId;
            ObjectNode node = objectMapper.createObjectNode();
            node.put("commandId", commandId.toString());
            node.put("tenantId", tenantId.toString());
            node.put("deviceId", deviceId.toString());
            node.put("type", commandType);
            node.put("payload", payload);
            kafkaTemplate.send("iot.commands", kafkaKey, objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish command to Kafka", e);
        }
        return cmd;
    }

    public List<Command> getCommands(UUID tenantId, UUID deviceId, int limit) {
        return commandRepository.findByTenantIdAndDeviceId(tenantId, deviceId, limit);
    }
}
