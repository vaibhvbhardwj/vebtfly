package com.example.vently.auth.dto;

import com.example.vently.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthenticationRequest {
    
    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Email must be a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
}
