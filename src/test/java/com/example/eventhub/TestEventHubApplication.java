package com.example.eventhub;

import org.springframework.boot.SpringApplication;

public class TestEventHubApplication {

    static void main(String[] args) {
        SpringApplication.from(EventHubApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
