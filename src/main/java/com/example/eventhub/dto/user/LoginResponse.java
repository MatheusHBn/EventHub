package com.example.eventhub.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "JWT access token used to authenticate requests", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token
) {
}
