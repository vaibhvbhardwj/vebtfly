package com.example.vently.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

/**
 * Service for sending SMS via AWS SNS
 */
@Service
public class SnsService {

    private final SnsClient snsClient;

    public SnsService(
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            @Value("${aws.s3.region}") String region) {

        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            this.snsClient = SnsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
        } else {
            this.snsClient = null;
        }
    }

    public boolean isConfigured() {
        return snsClient != null;
    }

    /**
     * Send an SMS message to a phone number
     * @param phoneNumber E.164 format e.g. +919876543210
     * @param message     The SMS body
     */
    public void sendSms(String phoneNumber, String message) {
        if (snsClient == null) {
            throw new RuntimeException("SNS service is not configured");
        }
        snsClient.publish(PublishRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .build());
    }
}
