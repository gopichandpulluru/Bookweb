package com.shopease.controller;

import com.shopease.dto.response.PageResponse;
import com.shopease.dto.response.ProductResponse;
import com.shopease.service.ProductService;
import com.shopease.util.Constants;
import com.shopease.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Set<String> SORT_FIELDS = Set.of("name", "price", "createdAt");

    private final ProductService productService;

    // GET /api/v1/products?keyword=mouse&categoryId=2&page=0&size=12&sortBy=price&direction=asc
    @GetMapping
    public PageResponse<ProductResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return productService.search(keyword, categoryId,
                PageUtil.of(page, size, sortBy, direction, SORT_FIELDS, "createdAt"));
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }
}
