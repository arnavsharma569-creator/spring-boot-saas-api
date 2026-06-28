package com.arnav.authsystem.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.arnav.authsystem.entities.UserInfo;
import com.arnav.authsystem.repository.UserRepository;
import com.arnav.authsystem.service.StripeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final StripeService stripeService;
    private final UserRepository userRepository;

    // ---- START checkout (PROTECTED — user must be logged in) ----
    // POST /api/create-checkout-session
    // returns { "url": "https://checkout.stripe.com/..." } for the frontend to redirect to
    @PostMapping("/api/create-checkout-session")
    public ResponseEntity<?> createCheckout(Authentication authentication) {
        try {
            String username = authentication.getName();   // from the JWT
            String checkoutUrl = stripeService.createCheckoutSession(username);
            return ResponseEntity.ok(Map.of("url", checkoutUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Could not start checkout: " + e.getMessage()));
        }
    }

    // ---- CONFIRM upgrade after Stripe redirects back (PROTECTED) ----
    // POST /api/confirm-upgrade   body: { "sessionId": "cs_test_..." }
    // verifies the payment with Stripe, then flips the user to PAID
    @PostMapping("/api/confirm-upgrade")
    public ResponseEntity<?> confirmUpgrade(@RequestBody Map<String, String> body,
                                            Authentication authentication) {
        try {
            String sessionId = body.get("sessionId");
            // ask Stripe: was this session actually paid? who paid?
            String paidUsername = stripeService.verifyAndGetUsername(sessionId);

            // security: the paid session must belong to the logged-in user
            String loggedInUser = authentication.getName();
            if (paidUsername == null || !paidUsername.equals(loggedInUser)) {
                return ResponseEntity.status(400)
                        .body(Map.of("error", "Payment not verified"));
            }

            // flip the tier to PAID
            UserInfo user = userRepository.findByUsername(loggedInUser)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setTier("PAID");
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("status", "upgraded", "tier", "PAID"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}