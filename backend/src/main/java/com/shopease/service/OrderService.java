package com.shopease.service;

import com.shopease.dto.request.PlaceOrderRequest;
import com.shopease.dto.response.OrderResponse;
import com.shopease.dto.response.PageResponse;
import com.shopease.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    // ----- customer -----
    OrderResponse placeOrder(String email, PlaceOrderRequest request);

    PageResponse<OrderResponse> getMyOrders(String email, Pageable pageable);

    OrderResponse getMyOrder(String email, Long orderId);

    OrderResponse cancelMyOrder(String email, Long orderId);

    // ----- admin -----
    PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable);

    OrderResponse getOrder(Long orderId);

    OrderResponse updateStatus(Long orderId, OrderStatus newStatus);

    // ----- scheduler -----
    /** Cancels PENDING orders older than the given number of hours and returns the stock. Returns how many. */
    int cancelUnpaidOrdersOlderThan(int hours);
}
