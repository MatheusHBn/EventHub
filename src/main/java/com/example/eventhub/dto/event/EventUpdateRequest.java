package com.example.eventhub.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventUpdateRequest(
        @Schema(description = "Updated title of the event", example = "Java Backend Conference 2026")
        @NotBlank
        @Size(max = 150)
        String title,
        @Schema(description = "Updated description of the event", example = "Updated conference description.")
        @NotBlank
        @Size(max = 2000)
        String description,
        @Schema(description = "Updated date and time of the event", example = "2026-12-20T14:00:00")
        LocalDateTime date,
        @NotBlank
        @Size(max = 255)
        @Schema(description = "Updated location of the event", example = "São Paulo Expo")
        String location,
        @Schema(description = "Updated maximum number of participants", example = "200", minimum = "1")
        @NotNull
        @Positive
        Integer capacity
) {
}
