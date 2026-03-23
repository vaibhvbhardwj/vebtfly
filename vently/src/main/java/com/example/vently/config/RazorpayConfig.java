package com.example.vently.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            if (razorpayKeyId == null || razorpayKeyId.isEmpty() || 
                razorpayKeySecret == null || razorpayKeySecret.isEmpty()) {
                log.warn("Razorpay credentials not configured. Payment features will be disabled.");
                return null;
            }
            
            log.info("Initializing Razorpay client with key: {}", razorpayKeyId);
            return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    public String razorpayKeySecret() {
        return razorpayKeySecret;
    }

    @Bean
    public String razorpayKeyId() {
        return razorpayKeyId;
    }
}