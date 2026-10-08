package com.shopease.dto.request;

import jakarta.validation.constraints.NotNull;

public record PaymentIntentRequest(
        @NotNull(message = "Order id is required")
        Long orderId
) {
}
