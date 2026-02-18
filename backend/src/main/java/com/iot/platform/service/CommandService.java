package com.iot.platform.service;

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

        // Publish to Kafka for the gateway to pick up and forward via MQTT
        String kafkaKey = tenantId + "|" + deviceId;
        String kafkaValue = String.format(
                "{\"commandId\":\"%s\",\"tenantId\":\"%s\",\"deviceId\":\"%s\",\"type\":\"%s\",\"payload\":%s}",
                commandId, tenantId, deviceId, commandType, payload);
        kafkaTemplate.send("iot.commands", kafkaKey, kafkaValue);
        return cmd;
    }

    public List<Command> getCommands(UUID tenantId, UUID deviceId, int limit) {
        return commandRepository.findByTenantIdAndDeviceId(tenantId, deviceId, limit);
    }
}
