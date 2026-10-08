package com.shopease.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotNull(message = "Shipping address is required")
        @Valid
        AddressRequest shippingAddress
) {
}
