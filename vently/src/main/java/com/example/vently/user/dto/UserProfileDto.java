package com.example.vently.user.dto;

import com.example.vently.user.AccountStatus;
import com.example.vently.user.Role;
import com.example.vently.validation.ValidEmail;
import com.example.vently.validation.ValidPhoneNumber;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    
    private Long id;
    
    @ValidEmail(message = "Email must be a valid email address")
    private String email;
    
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    private String gender;

    private LocalDate dateOfBirth;

    private Role role;
    
    // Common profile fields
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    @ValidPhoneNumber(message = "Phone number must be in valid format")
    private String phone;
    
    private Boolean phoneVerified;
    
    private String profilePictureUrl;
    private List<String> galleryPhotos;
    private Boolean verificationBadge;
    private Integer noShowCount;
    private AccountStatus accountStatus;
    
    // Volunteer-specific fields
    @Size(max = 500, message = "Skills must not exceed 500 characters")
    private String skills;
    
    @Size(max = 500, message = "Availability must not exceed 500 characters")
    private String availability;
    
    @Size(max = 1000, message = "Experience must not exceed 1000 characters")
    private String experience;
    
    // Organizer-specific fields
    @Size(min = 2, max = 200, message = "Organization name must be between 2 and 200 characters")
    private String organizationName;
    
    @Size(max = 1000, message = "Organization details must not exceed 1000 characters")
    private String organizationDetails;
    
    // Rating information
    private Double averageRating;
    private Long totalRatings;
}
