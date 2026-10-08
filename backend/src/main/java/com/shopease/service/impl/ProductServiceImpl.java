package com.shopease.service.impl;

import com.shopease.dto.request.ProductRequest;
import com.shopease.dto.response.PageResponse;
import com.shopease.dto.response.ProductResponse;
import com.shopease.entity.Category;
import com.shopease.entity.Product;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.mapper.ProductMapper;
import com.shopease.repository.CategoryRepository;
import com.shopease.repository.ProductRepository;
import com.shopease.service.ProductService;
import com.shopease.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Long categoryId, Pageable pageable) {
        // Start with "active products only", then add filters only if the client sent them
        Specification<Product> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("active"), true));

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }

        return PageResponse.from(productRepository.findAll(spec, pageable).map(productMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)   // hidden products look like "not found" to the public
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        Category category = findCategory(request.categoryId());

        Product product = Product.builder()
                .name(request.name().trim())
                .slug(uniqueSlug(request.name()))
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .active(true)
                .category(category)
                .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setName(request.name().trim());   // slug stays the same so existing links keep working
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setCategory(findCategory(request.categoryId()));
        if (request.active() != null) {
            product.setActive(request.active());
        }
        // No save() needed: the entity is managed, so changes are written when the transaction commits
        return productMapper.toResponse(product);
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    private String uniqueSlug(String name) {
        String base = SlugUtil.toSlug(name);
        if (base.isBlank()) {
            base = "product";
        }
        String slug = base;
        while (productRepository.existsBySlug(slug)) {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }
}
