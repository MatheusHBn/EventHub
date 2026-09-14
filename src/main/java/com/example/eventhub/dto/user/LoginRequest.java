package com.example.eventhub.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "User's registered email address", example = "matheus@test.com")
        @NotBlank
        String email,
        @Schema(description = "User's password", example = "password-test")
        @NotBlank
        String password
) {
}
