package com.shopease.service;

import com.shopease.dto.request.ReviewRequest;
import com.shopease.dto.response.PageResponse;
import com.shopease.dto.response.ReviewResponse;
import com.shopease.entity.Product;
import com.shopease.entity.Review;
import com.shopease.entity.User;
import com.shopease.exception.BadRequestException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.mapper.ReviewMapper;
import com.shopease.repository.ProductRepository;
import com.shopease.repository.ReviewRepository;
import com.shopease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    public ReviewResponse addReview(String email, Long productId, ReviewRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review saved = reviewRepository.save(Review.builder()
                .user(user)
                .product(product)
                .rating(request.rating())
                .comment(request.comment())
                .build());
        return reviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getForProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        return PageResponse.from(reviewRepository.findByProductId(productId, pageable).map(reviewMapper::toResponse));
    }
}
