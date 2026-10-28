package com.brandguard.api;

import com.brandguard.api.ApiModels.ErrorView;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorView> notFound(EntityNotFoundException e) {
        return response(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorView> badRequest(Exception e) {
        String message = e instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining(", "))
                : e.getMessage();
        return response(HttpStatus.BAD_REQUEST, message == null ? "Invalid request" : message);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorView> conflict(IllegalStateException e) {
        return response(HttpStatus.CONFLICT, e.getMessage());
    }

    private ResponseEntity<ErrorView> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorView(message, Instant.now()));
    }
}
