package com.example.vently.exception;

/**
 * Exception thrown when a user lacks the required permissions to access a resource.
 * Maps to HTTP 403 Forbidden.
 */
public class UnauthorizedException extends RuntimeException {
    private final String requiredRole;
    private final String userRole;

    public UnauthorizedException(String message) {
        super(message);
        this.requiredRole = null;
        this.userRole = null;
    }

    public UnauthorizedException(String message, String requiredRole, String userRole) {
        super(message);
        this.requiredRole = requiredRole;
        this.userRole = userRole;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public String getUserRole() {
        return userRole;
    }
}
