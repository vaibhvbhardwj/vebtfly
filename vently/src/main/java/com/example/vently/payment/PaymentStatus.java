package com.example.vently.payment;

public enum PaymentStatus {
    PENDING,      // Payment intent created, awaiting confirmation
    COMPLETED,    // Payment successfully processed and held in escrow
    REFUNDED,     // Payment refunded to organizer
    FAILED        // Payment processing failed
}
