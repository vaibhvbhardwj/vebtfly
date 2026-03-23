package com.example.vently.exception;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for custom exception classes.
 */
class CustomExceptionTest {

    @Test
    void testResourceNotFoundException_WithFieldDetails() {
        // Test ResourceNotFoundException with field details
        ResourceNotFoundException exception = new ResourceNotFoundException(
                "Event", "id", 123L
        );
        
        assertEquals("Event not found with id: '123'", exception.getMessage());
        assertEquals("Event", exception.getResourceName());
        assertEquals("id", exception.getFieldName());
        assertEquals(123L, exception.getFieldValue());
    }

    @Test
    void testResourceNotFoundException_WithMessage() {
        // Test ResourceNotFoundException with custom message
        ResourceNotFoundException exception = new ResourceNotFoundException(
                "Resource not found"
        );
        
        assertEquals("Resource not found", exception.getMessage());
        assertNull(exception.getResourceName());
        assertNull(exception.getFieldName());
        assertNull(exception.getFieldValue());
    }

    @Test
    void testUnauthorizedException_WithRoleDetails() {
        // Test UnauthorizedException with role details
        UnauthorizedException exception = new UnauthorizedException(
                "Access denied",
                "ADMIN",
                "VOLUNTEER"
        );
        
        assertEquals("Access denied", exception.getMessage());
        assertEquals("ADMIN", exception.getRequiredRole());
        assertEquals("VOLUNTEER", exception.getUserRole());
    }

    @Test
    void testUnauthorizedException_WithMessage() {
        // Test UnauthorizedException with custom message
        UnauthorizedException exception = new UnauthorizedException(
                "Unauthorized access"
        );
        
        assertEquals("Unauthorized access", exception.getMessage());
        assertNull(exception.getRequiredRole());
        assertNull(exception.getUserRole());
    }

    @Test
    void testValidationException_WithFieldErrors() {
        // Test ValidationException with field errors
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("email", "Invalid email format");
        fieldErrors.put("password", "Password too short");
        
        ValidationException exception = new ValidationException(
                "Validation failed",
                fieldErrors
        );
        
        assertEquals("Validation failed", exception.getMessage());
        assertEquals(2, exception.getFieldErrors().size());
        assertEquals("Invalid email format", exception.getFieldErrors().get("email"));
        assertEquals("Password too short", exception.getFieldErrors().get("password"));
        assertTrue(exception.hasFieldErrors());
    }

    @Test
    void testValidationException_AddFieldError() {
        // Test adding field errors dynamically
        ValidationException exception = new ValidationException("Validation failed");
        
        assertFalse(exception.hasFieldErrors());
        
        exception.addFieldError("email", "Invalid email");
        exception.addFieldError("phone", "Invalid phone");
        
        assertTrue(exception.hasFieldErrors());
        assertEquals(2, exception.getFieldErrors().size());
        assertEquals("Invalid email", exception.getFieldErrors().get("email"));
        assertEquals("Invalid phone", exception.getFieldErrors().get("phone"));
    }

    @Test
    void testValidationException_EmptyFieldErrors() {
        // Test ValidationException with empty field errors
        ValidationException exception = new ValidationException("Validation failed");
        
        assertFalse(exception.hasFieldErrors());
        assertTrue(exception.getFieldErrors().isEmpty());
    }

    @Test
    void testValidationException_NullFieldErrors() {
        // Test ValidationException with null field errors
        ValidationException exception = new ValidationException("Validation failed", null);
        
        assertFalse(exception.hasFieldErrors());
        assertTrue(exception.getFieldErrors().isEmpty());
    }

    @Test
    void testPaymentException_WithPaymentDetails() {
        // Test PaymentException with payment details
        PaymentException exception = new PaymentException(
                "Payment processing failed",
                "PAY_123",
                "INSUFFICIENT_FUNDS"
        );
        
        assertEquals("Payment processing failed", exception.getMessage());
        assertEquals("PAY_123", exception.getPaymentId());
        assertEquals("INSUFFICIENT_FUNDS", exception.getErrorCode());
    }

    @Test
    void testPaymentException_WithMessage() {
        // Test PaymentException with custom message
        PaymentException exception = new PaymentException(
                "Payment failed"
        );
        
        assertEquals("Payment failed", exception.getMessage());
        assertNull(exception.getPaymentId());
        assertNull(exception.getErrorCode());
    }

    @Test
    void testExceptionInheritance() {
        // Verify all custom exceptions extend RuntimeException
        assertTrue(new ResourceNotFoundException("test") instanceof RuntimeException);
        assertTrue(new UnauthorizedException("test") instanceof RuntimeException);
        assertTrue(new ValidationException("test") instanceof RuntimeException);
        assertTrue(new PaymentException("test") instanceof RuntimeException);
    }

    @Test
    void testExceptionStackTrace() {
        // Verify exceptions have proper stack traces
        try {
            throw new ResourceNotFoundException("Event", "id", 1L);
        } catch (ResourceNotFoundException e) {
            assertNotNull(e.getStackTrace());
            assertTrue(e.getStackTrace().length > 0);
        }
    }

    @Test
    void testValidationException_FieldErrorsImmutability() {
        // Test that field errors can be modified
        ValidationException exception = new ValidationException("Validation failed");
        exception.addFieldError("field1", "error1");
        
        Map<String, String> errors = exception.getFieldErrors();
        errors.put("field2", "error2");
        
        // Verify the exception's field errors are updated
        assertEquals(2, exception.getFieldErrors().size());
    }

    @Test
    void testResourceNotFoundException_VariousFieldValues() {
        // Test ResourceNotFoundException with various field value types
        ResourceNotFoundException exceptionWithLong = new ResourceNotFoundException(
                "Event", "id", 123L
        );
        assertEquals(123L, exceptionWithLong.getFieldValue());
        
        ResourceNotFoundException exceptionWithString = new ResourceNotFoundException(
                "User", "email", "test@example.com"
        );
        assertEquals("test@example.com", exceptionWithString.getFieldValue());
        
        ResourceNotFoundException exceptionWithInt = new ResourceNotFoundException(
                "Application", "id", 456
        );
        assertEquals(456, exceptionWithInt.getFieldValue());
    }
}
