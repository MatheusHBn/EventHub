package com.example.eventhub.dto.event;

import com.example.eventhub.domain.EventStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime date,
        String location,
        Integer capacity,
        EventStatus status,
        LocalDateTime createdAt,
        Long organizerId
) {
}
