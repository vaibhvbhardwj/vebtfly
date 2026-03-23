package com.example.vently.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user") // PostgreSQL reserved word fix
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private String gender;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Common profile fields
    @Column(length = 1000)
    private String bio;

    private String phone;

    private String profilePictureUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    private String phoneOtp;

    private LocalDateTime phoneOtpExpiresAt;

    @Column(columnDefinition = "TEXT")
    private String galleryPhotos; // JSON array of photo URLs

    @Column(nullable = false)
    @Builder.Default
    private Boolean verificationBadge = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer noShowCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    private LocalDateTime suspendedUntil;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    // Email verification fields
    private String verificationToken;
    
    private LocalDateTime verificationTokenExpiresAt;

    // Email OTP fields (6-digit code for registration verification)
    private String emailOtp;

    private LocalDateTime emailOtpExpiresAt;

    // Email notification preferences
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyOnApplicationStatus = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyOnEventCancellation = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyOnPayment = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyOnDisputeResolution = true;

    // Volunteer-specific fields
    @Column(length = 2000)
    private String skills;

    @Column(length = 1000)
    private String availability;

    @Column(length = 2000)
    private String experience;

    // Organizer-specific fields
    private String organizationName;

    @Column(length = 2000)
    private String organizationDetails;

    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Returns the role prefixed with ROLE_ (e.g., ROLE_ADMIN)
        // Default to VOLUNTEER if role is null (should not happen in normal flow)
        if (role == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_VOLUNTEER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // We use email as the unique identifier
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.BANNED && 
               (suspendedUntil == null || LocalDateTime.now().isAfter(suspendedUntil));
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Allow login if account is active, regardless of email verification status
        // Email verification is a separate concern from account enablement
        return accountStatus == AccountStatus.ACTIVE;
    }
}
