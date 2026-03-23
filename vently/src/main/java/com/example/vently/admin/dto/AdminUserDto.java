package com.example.vently.admin.dto;

import java.time.LocalDateTime;
import com.example.vently.user.AccountStatus;
import com.example.vently.user.Role;
import com.example.vently.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private AccountStatus accountStatus;
    private Boolean verificationBadge;
    private Boolean emailVerified;
    private Integer noShowCount;
    private LocalDateTime suspendedUntil;
    private LocalDateTime createdAt;

    // Convenience fields for frontend
    private boolean suspended;
    private boolean banned;
    private boolean verified;

    public static AdminUserDto from(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .verificationBadge(user.getVerificationBadge())
                .emailVerified(user.getEmailVerified())
                .noShowCount(user.getNoShowCount())
                .suspendedUntil(user.getSuspendedUntil())
                .createdAt(user.getCreatedAt())
                .suspended(user.getAccountStatus() == AccountStatus.SUSPENDED)
                .banned(user.getAccountStatus() == AccountStatus.BANNED)
                .verified(Boolean.TRUE.equals(user.getVerificationBadge()))
                .build();
    }
}
