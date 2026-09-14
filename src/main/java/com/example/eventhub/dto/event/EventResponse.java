package com.example.eventhub.dto.event;

import com.example.eventhub.domain.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventResponse(
        @Schema(description = "Unique identifier of the event", example = "1")
        Long id,
        @Schema(description = "Title of the event", example = "Java Backend Conference")
        String title,
        @Schema(description = "Description of the event", example = "A conference focused on Java, Spring Boot and backend development.")
        String description,
        @Schema(description = "Date and time when the event will take place", example = "2026-12-20T14:00:00")
        LocalDateTime date,
        @Schema(description = "Location of the event", example = "São Paulo Expo")
        String location,
        @Schema(description = "Maximum number of participants", example = "100")
        Integer capacity,
        @Schema(description = "Current status of the event", example = "PUBLISHED")
        EventStatus status,
        @Schema(description = "Date and time when the event was created", example = "2026-09-14T12:30:00")
        LocalDateTime createdAt,
        @Schema(description = "Unique identifier of the event organizer", example = "5")
        Long organizerId
) {
}
