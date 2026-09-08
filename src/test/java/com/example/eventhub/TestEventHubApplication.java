package com.example.eventhub;

import org.springframework.boot.SpringApplication;

public class TestEventHubApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventHubApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
