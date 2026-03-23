package com.example.vently.dispute;

/**
 * Enum representing the lifecycle status of a dispute.
 * 
 * Status flow:
 * OPEN -> UNDER_REVIEW -> RESOLVED/CLOSED
 */
public enum DisputeStatus {
    /**
     * Dispute has been created and is awaiting admin review
     */
    OPEN,
    
    /**
     * Admin is actively reviewing the dispute
     */
    UNDER_REVIEW,
    
    /**
     * Dispute has been resolved by admin with a decision
     */
    RESOLVED,
    
    /**
     * Dispute has been closed without resolution
     */
    CLOSED
}
