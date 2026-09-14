package com.example.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlreadyCanceledException extends ResponseStatusException {
    public AlreadyCanceledException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
