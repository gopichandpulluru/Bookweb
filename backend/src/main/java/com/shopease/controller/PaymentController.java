package com.shopease.controller;

import com.shopease.dto.request.PaymentIntentRequest;
import com.shopease.dto.response.PaymentIntentResponse;
import com.shopease.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Called by the React checkout page (user is logged in)
    @PostMapping("/create-intent")
    public PaymentIntentResponse createIntent(@AuthenticationPrincipal UserDetails user,
                                              @Valid @RequestBody PaymentIntentRequest request) {
        return paymentService.createPaymentIntent(user.getUsername(), request.orderId());
    }

    // Called by Stripe's servers (no JWT). Public in SecurityConfig, protected by the signature check instead.
    // The body must be the RAW string, otherwise the signature check fails.
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
