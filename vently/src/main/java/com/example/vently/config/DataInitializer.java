package com.example.vently.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.vently.user.AccountStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no users exist
        if (userRepository.count() > 0) {
            log.info("Database already has users, skipping initialization");
            return;
        }

        log.info("Initializing test data...");

        // Create test volunteer
        User volunteer = User.builder()
                .email("volunteer@test.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Test Volunteer")
                .role(Role.VOLUNTEER)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(false)
                .emailNotificationsEnabled(true)
                .notifyOnApplicationStatus(true)
                .notifyOnEventCancellation(true)
                .notifyOnPayment(true)
                .notifyOnDisputeResolution(true)
                .build();

        // Create test organizer
        User organizer = User.builder()
                .email("organizer@test.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Test Organizer")
                .role(Role.ORGANIZER)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(false)
                .emailNotificationsEnabled(true)
                .notifyOnApplicationStatus(true)
                .notifyOnEventCancellation(true)
                .notifyOnPayment(true)
                .notifyOnDisputeResolution(true)
                .organizationName("Test Organization")
                .build();

        // Create test admin
        User admin = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Test Admin")
                .role(Role.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(false)
                .emailNotificationsEnabled(true)
                .notifyOnApplicationStatus(true)
                .notifyOnEventCancellation(true)
                .notifyOnPayment(true)
                .notifyOnDisputeResolution(true)
                .build();

        userRepository.save(volunteer);
        userRepository.save(organizer);
        userRepository.save(admin);

        log.info("Test data initialized successfully");
        log.info("Test users created:");
        log.info("  - volunteer@test.com / password123 (VOLUNTEER)");
        log.info("  - organizer@test.com / password123 (ORGANIZER)");
        log.info("  - admin@test.com / password123 (ADMIN)");
    }
}
