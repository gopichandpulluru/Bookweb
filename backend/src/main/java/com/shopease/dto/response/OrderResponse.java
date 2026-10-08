package com.shopease.dto.response;

import com.shopease.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        String customerEmail,
        AddressResponse shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
}
