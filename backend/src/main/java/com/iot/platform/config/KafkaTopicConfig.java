package com.iot.platform.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic telemetryTopic() {
        return TopicBuilder.name("iot.telemetry").partitions(12).replicas(1).build();
    }

    @Bean
    public NewTopic deviceStateTopic() {
        return TopicBuilder.name("iot.device-state").partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic eventsTopic() {
        return TopicBuilder.name("iot.events").partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic commandsTopic() {
        return TopicBuilder.name("iot.commands").partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic commandAcksTopic() {
        return TopicBuilder.name("iot.command-acks").partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic alarmsTopic() {
        return TopicBuilder.name("iot.alarms").partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic auditTopic() {
        return TopicBuilder.name("iot.audit").partitions(3).replicas(1).build();
    }
}
