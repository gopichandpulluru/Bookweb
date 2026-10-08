package com.shopease.controller;

import com.shopease.dto.request.PlaceOrderRequest;
import com.shopease.dto.response.OrderResponse;
import com.shopease.dto.response.PageResponse;
import com.shopease.service.OrderService;
import com.shopease.util.Constants;
import com.shopease.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Turns the current cart into an order
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal UserDetails user,
                                                    @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(user.getUsername(), request));
    }

    @GetMapping
    public PageResponse<OrderResponse> myOrders(@AuthenticationPrincipal UserDetails user,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        return orderService.getMyOrders(user.getUsername(),
                PageUtil.of(page, size, "createdAt", "desc", Set.of("createdAt"), "createdAt"));
    }

    @GetMapping("/{id}")
    public OrderResponse myOrder(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        return orderService.getMyOrder(user.getUsername(), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        return orderService.cancelMyOrder(user.getUsername(), id);
    }
}
