package com.shopease.service;

import com.shopease.entity.Product;
import com.shopease.exception.BadRequestException;
import com.shopease.exception.OutOfStockException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** The only place that changes product stock, so the rules live in one spot. */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    /**
     * Takes items out of stock. Runs inside the caller's transaction (OrderService.placeOrder),
     * so if anything later fails, the stock change is rolled back too.
     */
    @Transactional
    public Product reserve(Long productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId)   // row lock
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.isActive()) {
            throw new BadRequestException("'" + product.getName() + "' is no longer available");
        }
        if (product.getStock() < quantity) {
            throw new OutOfStockException(product.getName(), quantity, product.getStock());
        }
        product.setStock(product.getStock() - quantity);
        return product;
    }

    /** Puts items back (cancelled order). */
    @Transactional
    public void restock(Long productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setStock(product.getStock() + quantity);
    }

    @Transactional(readOnly = true)
    public List<Product> findLowStock(int threshold) {
        return productRepository.findByActiveTrueAndStockLessThanEqual(threshold);
    }
}
