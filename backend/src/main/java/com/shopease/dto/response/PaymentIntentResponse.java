package com.shopease.dto.response;

import java.math.BigDecimal;

public record PaymentIntentResponse(
        String clientSecret,      // the React app gives this to Stripe.js to confirm the payment
        String paymentIntentId,
        BigDecimal amount,
        String currency
) {
}
