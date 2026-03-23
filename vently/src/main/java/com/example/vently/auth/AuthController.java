package com.example.vently.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vently.auth.dto.AuthenticationRequest;
import com.example.vently.auth.dto.AuthenticationResponse;
import com.example.vently.auth.dto.RegisterRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/send-email-otp")
    public ResponseEntity<Void> sendEmailOtp(@RequestBody java.util.Map<String, String> body) {
        service.sendEmailOtp(body.get("email"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email-otp")
    public ResponseEntity<Void> verifyEmailOtp(@RequestBody java.util.Map<String, String> body) {
        service.verifyEmailOtp(body.get("email"), body.get("otp"));
        return ResponseEntity.ok().build();
    }
}
