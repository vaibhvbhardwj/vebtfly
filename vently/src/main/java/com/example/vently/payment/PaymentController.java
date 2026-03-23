package com.example.vently.payment;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vently.payment.dto.ConfirmDepositDto;
import com.example.vently.payment.dto.DepositRequestDto;
import com.example.vently.payment.dto.PaymentResponseDto;
import com.example.vently.payment.dto.PayoutResponseDto;
import com.example.vently.user.User;
import com.example.vently.user.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    /**
     * Create deposit intent for an event
     * Only organizers can create deposits
     */
    @PostMapping("/deposit")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Map<String, String>> createDepositIntent(
            @Valid @RequestBody DepositRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String clientSecret = paymentService.createDepositIntent(request.getEventId());

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", clientSecret);
        response.put("message", "Deposit intent created successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Confirm deposit after successful payment
     * Only organizers can confirm deposits
     */
    @PostMapping("/confirm")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Map<String, String>> confirmDeposit(
            @Valid @RequestBody ConfirmDepositDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        paymentService.confirmDeposit(request.getEventId(), request.getRazorpayPaymentId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Deposit confirmed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get payment history for the authenticated organizer
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Page<PaymentResponseDto>> getPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> payments = paymentService.getPaymentHistory(user.getId(), pageable);

        Page<PaymentResponseDto> response = payments.map(PaymentResponseDto::fromEntity);

        return ResponseEntity.ok(response);
    }

    /**
     * Get payout history for the authenticated volunteer
     */
    @GetMapping("/payouts")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<Page<PayoutResponseDto>> getPayoutHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payout> payouts = paymentService.getPayoutHistory(user.getId(), pageable);

        Page<PayoutResponseDto> response = payouts.map(PayoutResponseDto::fromEntity);

        return ResponseEntity.ok(response);
    }
}
