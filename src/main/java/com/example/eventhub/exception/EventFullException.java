package com.example.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EventFullException extends ResponseStatusException {
    public EventFullException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
