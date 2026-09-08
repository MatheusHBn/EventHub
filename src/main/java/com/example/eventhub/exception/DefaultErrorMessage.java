package com.example.eventhub.exception;

public record DefaultErrorMessage(
        int status,
        String message

) {
}
