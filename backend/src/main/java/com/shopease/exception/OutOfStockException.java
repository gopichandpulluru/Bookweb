package com.shopease.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }

    // Usage: throw new OutOfStockException(product.getName(), requested, available);
    public OutOfStockException(String productName, int requested, int available) {
        super(String.format("Insufficient stock for '%s': requested %d, available %d",
                productName, requested, available));
    }
}
