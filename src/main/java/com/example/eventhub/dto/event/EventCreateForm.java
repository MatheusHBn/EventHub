package com.example.eventhub.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventCreateForm(
        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        @Size(max = 2000)
        String description,

        LocalDateTime date,

        @NotBlank
        @Size(max = 255)
        String location,

        @NotNull
        @Positive
        Integer capacity
) {
}
