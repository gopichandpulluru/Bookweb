package com.shopease.controller;

import com.shopease.dto.request.CartItemRequest;
import com.shopease.dto.request.UpdateCartItemRequest;
import com.shopease.dto.response.CartResponse;
import com.shopease.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserDetails user) {
        return cartService.getCart(user.getUsername());
    }

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal UserDetails user,
                                @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(user.getUsername(), request);
    }

    @PatchMapping("/items/{productId}")
    public CartResponse updateQuantity(@AuthenticationPrincipal UserDetails user,
                                       @PathVariable Long productId,
                                       @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(user.getUsername(), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@AuthenticationPrincipal UserDetails user, @PathVariable Long productId) {
        return cartService.removeItem(user.getUsername(), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal UserDetails user) {
        cartService.clear(user.getUsername());
    }
}
