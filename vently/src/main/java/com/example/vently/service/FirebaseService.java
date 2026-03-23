package com.example.vently.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class FirebaseService {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount =
                        new ClassPathResource("firebase-service-account.json").getInputStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }

    /**
     * Verifies a Firebase ID token and returns the phone number from it.
     * @param idToken the Firebase ID token from the frontend
     * @return the verified phone number in E.164 format
     */
    public String verifyIdTokenAndGetPhone(String idToken) {
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String phone = decoded.getClaims().get("phone_number") != null
                    ? decoded.getClaims().get("phone_number").toString()
                    : null;
            if (phone == null || phone.isBlank()) {
                throw new RuntimeException("No phone number in Firebase token");
            }
            return phone;
        } catch (Exception e) {
            log.error("Firebase token verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Firebase token: " + e.getMessage());
        }
    }
}
