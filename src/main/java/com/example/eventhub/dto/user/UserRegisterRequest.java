package com.example.eventhub.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRegisterRequest(
        @Schema(description = "User's full name", example = "Matheus Silva", minLength = 2, maxLength = 100)
        @NotBlank
        @Size(min = 4, max = 100)
        String name,
        @Schema(description = "User's email address", example = "matheus@email.com")
        @NotBlank
        @Email
        String email,
        @Schema(description = "User's password", example = "12345678", minLength = 8, maxLength = 100)
        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}
