package com.example.vently.subscription;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vently.payment.RazorpayService;
import com.example.vently.subscription.dto.CreateSubscriptionOrderRequest;
import com.example.vently.subscription.dto.CreateSubscriptionOrderResponse;
import com.example.vently.subscription.dto.SubscriptionDto;
import com.example.vently.subscription.dto.VerifySubscriptionPaymentRequest;
import com.example.vently.user.User;
import com.razorpay.Order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final RazorpayService razorpayService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionDto> getCurrentSubscription(@AuthenticationPrincipal User user) {
        Subscription sub = subscriptionService.getCurrentSubscription(user.getId());
        return ResponseEntity.ok(toDto(sub));
    }

    /**
     * Create Razorpay order for a given tier.
     * The tier is passed in the request body so the frontend can request GOLD or PLATINUM.
     */
    @PostMapping("/create-order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreateSubscriptionOrderResponse> createSubscriptionOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateSubscriptionOrderRequest request) {

        try {
            SubscriptionTier tier = parseTier(request.getTier());
            if (tier == SubscriptionTier.FREE) {
                return ResponseEntity.badRequest().build();
            }

            int priceInPaise = subscriptionService.getPriceInPaise(user.getRole(), tier);
            BigDecimal amountInRupees = BigDecimal.valueOf(priceInPaise).divide(BigDecimal.valueOf(100));

            String receipt = "sub_" + user.getId() + "_" + tier.name().toLowerCase() + "_" + System.currentTimeMillis();
            Order order = razorpayService.createOrder(amountInRupees, "INR", receipt);

            CreateSubscriptionOrderResponse response = CreateSubscriptionOrderResponse.builder()
                .orderId(order.get("id"))
                .razorpayKeyId(razorpayKeyId)
                .amount(amountInRupees)
                .currency("INR")
                .description("Vently " + tier.name().charAt(0) + tier.name().substring(1).toLowerCase() + " Plan")
                .userEmail(user.getEmail())
                .userName(user.getFullName())
                .userPhone(user.getPhone())
                .tier(tier.name())
                .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating subscription order for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verify payment and upgrade subscription to the requested tier.
     */
    @PostMapping("/verify-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionDto> verifyPaymentAndUpgrade(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VerifySubscriptionPaymentRequest request) {

        try {
            boolean isValid = razorpayService.verifyPaymentSignature(
                request.getOrderId(), request.getPaymentId(), request.getSignature());

            if (!isValid) {
                log.error("Payment signature verification failed for user: {}", user.getId());
                return ResponseEntity.badRequest().build();
            }

            SubscriptionTier tier = parseTier(request.getTier());
            Subscription sub = subscriptionService.upgradeSubscription(user.getId(), tier, request.getPaymentId());

            log.info("Upgraded user {} to {} plan", user.getId(), tier);
            return ResponseEntity.ok(toDto(sub));

        } catch (Exception e) {
            log.error("Error verifying payment for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/downgrade")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionDto> downgradeSubscription(@AuthenticationPrincipal User user) {
        Subscription sub = subscriptionService.downgradeSubscription(user.getId());
        return ResponseEntity.ok(toDto(sub));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private SubscriptionTier parseTier(String tier) {
        try {
            return SubscriptionTier.valueOf(tier.toUpperCase());
        } catch (Exception e) {
            return SubscriptionTier.FREE;
        }
    }

    private SubscriptionDto toDto(Subscription sub) {
        return SubscriptionDto.builder()
            .id(sub.getId())
            .userId(sub.getUser().getId())
            .tier(sub.getTier())
            .startDate(sub.getStartDate())
            .endDate(sub.getEndDate())
            .active(sub.getActive())
            .razorpayPaymentId(sub.getRazorpayPaymentId())
            .eventLimit(subscriptionService.getOrganizerEventLimit(sub.getTier()))
            .applicationLimit(subscriptionService.getVolunteerApplicationLimit(sub.getTier()))
            .build();
    }
}
