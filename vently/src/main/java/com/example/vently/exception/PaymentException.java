package com.example.vently.exception;

/**
 * Exception thrown when payment processing fails.
 * Maps to HTTP 400 Bad Request.
 */
public class PaymentException extends RuntimeException {
    private final String paymentId;
    private final String errorCode;

    public PaymentException(String message) {
        super(message);
        this.paymentId = null;
        this.errorCode = null;
    }

    public PaymentException(String message, String paymentId, String errorCode) {
        super(message);
        this.paymentId = paymentId;
        this.errorCode = errorCode;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
