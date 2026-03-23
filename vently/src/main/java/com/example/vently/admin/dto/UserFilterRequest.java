package com.example.vently.admin.dto;

import com.example.vently.user.AccountStatus;
import com.example.vently.user.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    private Role role;
    private AccountStatus status;
    private Boolean verificationBadge;
    private Integer page;
    private Integer size;
}
