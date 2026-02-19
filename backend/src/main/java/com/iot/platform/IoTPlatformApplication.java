package com.iot.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IoTPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(IoTPlatformApplication.class, args);
    }
}
