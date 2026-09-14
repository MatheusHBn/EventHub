package com.example.eventhub.dto.registration;

import com.example.eventhub.domain.RegistrationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RegistrationResponse(
        @Schema(description = "Unique identifier of the registration", example = "1")
        Long id,
        @Schema(description = "Unique identifier of the registered event", example = "10")
        Long eventId,
        @Schema(description = "Unique identifier of the registered user", example = "5")
        Long userId,
        @Schema(description = "Current status of the registration", example = "ACTIVE")
        RegistrationStatus status,
        @Schema(description = "Date and time when the registration was created", example = "2026-09-14T13:00:00")
        LocalDateTime registeredAt
) {
}
