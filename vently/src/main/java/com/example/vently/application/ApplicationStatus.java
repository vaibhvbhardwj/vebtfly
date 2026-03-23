package com.example.vently.application;

public enum ApplicationStatus {
    PENDING,      // Initial state when volunteer applies
    ACCEPTED,     // Organizer accepts the application (48-hour confirmation window starts)
    CONFIRMED,    // Volunteer confirms within 48 hours
    DECLINED,     // Volunteer declines or confirmation expires
    REJECTED,     // Organizer rejects the application
    CANCELLED     // Event is cancelled
}
