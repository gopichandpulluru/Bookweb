package com.shopease.controller;

import com.shopease.dto.request.ReviewRequest;
import com.shopease.dto.response.PageResponse;
import com.shopease.dto.response.ReviewResponse;
import com.shopease.service.ReviewService;
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
@RequestMapping("/api/v1/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // GET is public (all GETs under /products/** are open in SecurityConfig)
    @GetMapping
    public PageResponse<ReviewResponse> list(@PathVariable Long productId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        return reviewService.getForProduct(productId,
                PageUtil.of(page, size, "createdAt", "desc", Set.of("createdAt"), "createdAt"));
    }

    // POST needs a logged-in user
    @PostMapping
    public ResponseEntity<ReviewResponse> add(@AuthenticationPrincipal UserDetails user,
                                              @PathVariable Long productId,
                                              @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(user.getUsername(), productId, request));
    }
}
