package com.example.vently.payment;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import com.razorpay.Transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service for handling Razorpay payment operations
 * Razorpay is a popular payment gateway in India supporting UPI, cards, netbanking, and wallets
 */
@Service
@Slf4j
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final String razorpayKeySecret;
    
    public RazorpayService(@Autowired(required = false) RazorpayClient razorpayClient, 
                          @Autowired(required = false) String razorpayKeySecret) {
        this.razorpayClient = razorpayClient;
        this.razorpayKeySecret = razorpayKeySecret;
    }

    /**
     * Create a Razorpay order for deposit
     * Orders are used to collect payments from customers
     * 
     * @param amount   Amount in INR (will be converted to paise - smallest unit)
     * @param currency Currency code (default: "INR")
     * @param receipt  Receipt ID for tracking
     * @return Order object containing order_id and other details
     * @throws RazorpayException if Razorpay API call fails
     */
    public Order createOrder(BigDecimal amount, String currency, String receipt) throws RazorpayException {
        if (razorpayClient == null) {
            throw new RazorpayException("Razorpay client not configured. Please set credentials.");
        }
        
        log.info("Creating Razorpay order for amount: {} {}, receipt: {}", new Object[]{amount, currency, receipt});
        
        JSONObject orderRequest = new JSONObject();
        // Convert amount to paise (1 INR = 100 paise)
        orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
        orderRequest.put("currency", currency != null ? currency : "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1); // Auto-capture payment
        
        Order order = razorpayClient.orders.create(orderRequest);
        log.info("Razorpay order created: {}", new Object[]{order.get("id")});
        
        return order;
    }

    /**
     * Fetch payment details by payment ID
     * 
     * @param paymentId Razorpay payment ID
     * @return Payment object
     * @throws RazorpayException if Razorpay API call fails
     */
    public Payment fetchPayment(String paymentId) throws RazorpayException {
        if (razorpayClient == null) {
            throw new RazorpayException("Razorpay client not configured. Please set credentials.");
        }
        
        log.info("Fetching payment details: {}", new Object[]{paymentId});
        Payment payment = razorpayClient.payments.fetch(paymentId);
        log.info("Payment status: {}", new Object[]{payment.get("status")});
        
        return payment;
    }

    /**
     * Create a transfer to volunteer's linked account
     * Requires Razorpay Route (contact Razorpay to enable)
     * 
     * @param accountId Razorpay linked account ID
     * @param amount    Amount in INR
     * @param currency  Currency code (default: "INR")
     * @return Transfer object
     * @throws RazorpayException if Razorpay API call fails
     */
    public Transfer createTransfer(String accountId, BigDecimal amount, String currency) throws RazorpayException {
        if (razorpayClient == null) {
            throw new RazorpayException("Razorpay client not configured. Please set credentials.");
        }
        
        log.info("Creating transfer to account: {} for amount: {} {}", new Object[]{accountId, amount, currency});
        
        JSONObject transferRequest = new JSONObject();
        transferRequest.put("account", accountId);
        // Convert amount to paise
        transferRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
        transferRequest.put("currency", currency != null ? currency : "INR");
        
        Transfer transfer = razorpayClient.transfers.create(transferRequest);
        log.info("Transfer created: {}", new Object[]{transfer.get("id")});
        
        return transfer;
    }

    /**
     * Create a refund for a payment
     * 
     * @param paymentId Razorpay payment ID
     * @param amount    Amount to refund in INR (null for full refund)
     * @return Refund object
     * @throws RazorpayException if Razorpay API call fails
     */
    public Refund createRefund(String paymentId, BigDecimal amount) throws RazorpayException {
        if (razorpayClient == null) {
            throw new RazorpayException("Razorpay client not configured. Please set credentials.");
        }
        
        log.info("Creating refund for payment: {} amount: {}", new Object[]{paymentId, amount});
        
        JSONObject refundRequest = new JSONObject();
        
        if (amount != null) {
            // Convert amount to paise for partial refund
            refundRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
        }
        // If amount is null, Razorpay will refund the full amount
        
        // Fetch payment first to create refund
        Payment payment = razorpayClient.payments.fetch(paymentId);
        // Use RazorpayClient to create refund instead of Payment.createRefund()
        Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
        
        log.info("Refund created: {}", new Object[]{refund.get("id")});
        
        return refund;
    }

    /**
     * Verify payment signature for security
     * This should be called after receiving payment callback from frontend
     * 
     * @param orderId         Razorpay order ID
     * @param paymentId       Razorpay payment ID
     * @param signature       Signature from Razorpay callback
     * @return true if signature is valid
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        if (razorpayClient == null) {
            log.error("Razorpay client not configured");
            return false;
        }
        
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);
            
            // Razorpay utility method to verify signature
            com.razorpay.Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
            log.info("Payment signature verified successfully for order: {}", new Object[]{orderId});
            return true;
        } catch (RazorpayException e) {
            log.error("Payment signature verification failed: {}", new Object[]{e.getMessage()});
            return false;
        }
    }
}
