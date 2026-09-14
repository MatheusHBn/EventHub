package com.example.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<DefaultErrorMessage> handleNotFoundException(NotFoundException e) {
        DefaultErrorMessage defaultErrorMessage = new DefaultErrorMessage(HttpStatus.NOT_FOUND.value(), e.getReason());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(defaultErrorMessage);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<DefaultErrorMessage> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        DefaultErrorMessage defaultErrorMessage = new DefaultErrorMessage(HttpStatus.CONFLICT.value(), e.getReason());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(defaultErrorMessage);
    }

    @ExceptionHandler(EventFullException.class)
    public ResponseEntity<DefaultErrorMessage> handleEventFullException(EventFullException e) {
        DefaultErrorMessage defaultErrorMessage = new DefaultErrorMessage(HttpStatus.CONFLICT.value(), e.getReason());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(defaultErrorMessage);
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<DefaultErrorMessage> handleAlreadyRegisteredException(AlreadyRegisteredException e) {
        DefaultErrorMessage defaultErrorMessage = new DefaultErrorMessage(HttpStatus.CONFLICT.value(), e.getReason());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(defaultErrorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DefaultErrorMessage> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        DefaultErrorMessage defaultErrorMessage = new DefaultErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(defaultErrorMessage);
    }

    @ExceptionHandler(EventIsNotPublishedException.class)
    public ResponseEntity<DefaultErrorMessage> handleEventIsNotPublishedException(EventIsNotPublishedException e) {
        DefaultErrorMessage defaultErrorMessage = new DefaultErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(defaultErrorMessage);
    }

    @ExceptionHandler(AlreadyCanceledException.class)
    public ResponseEntity<DefaultErrorMessage> handleAlreadyCanceledException(AlreadyCanceledException e) {
        DefaultErrorMessage defaultErrorMessage = new DefaultErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(defaultErrorMessage);
    }

}
