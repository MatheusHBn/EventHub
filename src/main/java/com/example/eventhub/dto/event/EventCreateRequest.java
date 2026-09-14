package com.example.eventhub.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventCreateRequest(
        @Schema(description = "Title of the event", example = "Java Backend Conference")
        @NotBlank
        @Size(max = 150)
        String title,
        @Schema(description = "Detailed description of the event", example = "A conference focused on Java, Spring Boot and backend development.")
        @NotBlank
        @Size(max = 2000)
        String description,
        @Schema(description = "Date and time when the event will take place", example = "2026-12-20T14:00:00")
        LocalDateTime date,
        @NotBlank
        @Size(max = 255)
        @Schema(description = "Location where the event will take place", example = "São Paulo Expo")
        String location,
        @Schema(description = "Maximum number of participants", example = "100", minimum = "1")
        @NotNull
        @Positive
        Integer capacity
) {
}
