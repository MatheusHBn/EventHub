package com.example.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EventIsNotPublishedException extends ResponseStatusException {
    public EventIsNotPublishedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
