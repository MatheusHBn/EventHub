package com.example.eventhub.dto.user;

import com.example.eventhub.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        @Schema(description = "Unique identifier of the user", example = "1")
        Long id,
        @Schema(description = "User's full name", example = "Matheus Silva")
        String name,
        @Schema(description = "User's email address", example = "matheus@email.com")
        String email,
        @Schema(description = "User's role", example = "USER")
        Role role,
        @Schema(description = "Date and time when the user was created", example = "2026-09-14T12:30:00")
        LocalDateTime createdAt
) {
}
