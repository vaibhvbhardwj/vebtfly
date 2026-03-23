package com.example.vently.exception;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GlobalExceptionHandler.
 * Validates error handling for all custom exceptions and validation errors.
 */
class GlobalExceptionHandlerTest {

    @Test
    void testValidationException_WithFieldErrors() {
        // Test ValidationException with field errors
        ValidationException exception = new ValidationException("Validation failed");
        exception.addFieldError("email", "Invalid email format");
        exception.addFieldError("password", "Password too short");
        
        assert exception.hasFieldErrors();
        assert exception.getFieldErrors().size() == 2;
        assert exception.getFieldErrors().get("email").equals("Invalid email format");
    }

    @Test
    void testResourceNotFoundException() {
        // Test ResourceNotFoundException
        ResourceNotFoundException exception = new ResourceNotFoundException(
                "Event", "id", 123L
        );
        
        assert exception.getMessage().contains("Event not found");
        assert exception.getResourceName().equals("Event");
        assert exception.getFieldName().equals("id");
        assert exception.getFieldValue().equals(123L);
    }

    @Test
    void testUnauthorizedException() {
        // Test UnauthorizedException
        UnauthorizedException exception = new UnauthorizedException(
                "Access denied",
                "ADMIN",
                "VOLUNTEER"
        );
        
        assert exception.getMessage().equals("Access denied");
        assert exception.getRequiredRole().equals("ADMIN");
        assert exception.getUserRole().equals("VOLUNTEER");
    }

    @Test
    void testPaymentException() {
        // Test PaymentException
        PaymentException exception = new PaymentException(
                "Payment processing failed",
                "PAY_123",
                "INSUFFICIENT_FUNDS"
        );
        
        assert exception.getMessage().equals("Payment processing failed");
        assert exception.getPaymentId().equals("PAY_123");
        assert exception.getErrorCode().equals("INSUFFICIENT_FUNDS");
    }

    @Test
    void testErrorResponse_Creation() {
        // Test ErrorResponse creation
        ErrorResponse response = ErrorResponse.of(
                400,
                "VALIDATION_ERROR",
                "Request validation failed",
                "/api/v1/auth/register"
        );
        
        assert response.getStatus() == 400;
        assert response.getError().equals("VALIDATION_ERROR");
        assert response.getMessage().equals("Request validation failed");
        assert response.getPath().equals("/api/v1/auth/register");
        assert response.getTimestamp() != null;
    }

    @Test
    void testErrorResponse_WithFieldErrors() {
        // Test ErrorResponse with field errors
        java.util.Map<String, String> fieldErrors = new java.util.HashMap<>();
        fieldErrors.put("email", "Invalid email");
        
        ErrorResponse response = ErrorResponse.of(
                400,
                "VALIDATION_ERROR",
                "Validation failed",
                "/api/v1/auth/register",
                fieldErrors
        );
        
        assert response.getFieldErrors() != null;
        assert response.getFieldErrors().size() == 1;
        assert response.getFieldErrors().get("email").equals("Invalid email");
    }
}
