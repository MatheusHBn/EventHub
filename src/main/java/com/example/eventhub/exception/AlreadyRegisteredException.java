package com.example.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlreadyRegisteredException extends ResponseStatusException {
    public AlreadyRegisteredException(String message) {
        super(HttpStatus.CONFLICT,message);
    }
}
