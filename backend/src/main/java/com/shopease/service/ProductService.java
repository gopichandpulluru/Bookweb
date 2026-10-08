package com.shopease.service;

import com.shopease.dto.request.ProductRequest;
import com.shopease.dto.response.PageResponse;
import com.shopease.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    PageResponse<ProductResponse> search(String keyword, Long categoryId, Pageable pageable);

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    /** Soft delete: the product is hidden from the shop but old orders can still point to it. */
    void delete(Long id);
}
