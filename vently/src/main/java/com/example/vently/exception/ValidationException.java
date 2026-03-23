package com.example.vently.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when request validation fails.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }

    public void addFieldError(String fieldName, String errorMessage) {
        this.fieldErrors.put(fieldName, errorMessage);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
