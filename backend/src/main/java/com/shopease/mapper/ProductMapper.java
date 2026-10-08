package com.shopease.mapper;

import com.shopease.dto.response.ProductResponse;
import com.shopease.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSlug(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getImageUrl(),
                p.isActive(),
                p.getCategory().getId(),
                p.getCategory().getName());
    }
}
