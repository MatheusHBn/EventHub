package com.example.eventhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventHub API")
                        .description("API for event and registration management")
                        .version("v1.0.0"));
    }
}
