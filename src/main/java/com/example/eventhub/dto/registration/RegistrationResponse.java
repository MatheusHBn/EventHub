package com.example.eventhub.dto.registration;

import com.example.eventhub.domain.RegistrationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RegistrationResponse(
        Long id,
        Long eventId,
        Long userId,
        RegistrationStatus status,
        LocalDateTime registeredAt
) {
}
