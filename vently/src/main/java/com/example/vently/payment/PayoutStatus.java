package com.example.vently.payment;

public enum PayoutStatus {
    PENDING,      // Payout scheduled, awaiting processing
    COMPLETED,    // Payout successfully transferred to volunteer
    FAILED        // Payout failed (will retry up to 3 times)
}
