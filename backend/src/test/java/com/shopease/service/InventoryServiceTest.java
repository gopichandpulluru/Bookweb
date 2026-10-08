package com.shopease.service;

import com.shopease.entity.Product;
import com.shopease.exception.OutOfStockException;
import com.shopease.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock ProductRepository productRepository;
    @InjectMocks InventoryService inventoryService;

    @Test
    void reserve_reducesStock() {
        Product product = Product.builder().id(1L).name("Mouse").stock(10).active(true).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        inventoryService.reserve(1L, 3);

        assertThat(product.getStock()).isEqualTo(7);
    }

    @Test
    void reserve_throwsWhenNotEnoughStock() {
        Product product = Product.builder().id(1L).name("Mouse").stock(2).active(true).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> inventoryService.reserve(1L, 5))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("requested 5, available 2");
        assertThat(product.getStock()).isEqualTo(2);   // unchanged
    }

    @Test
    void restock_addsStockBack() {
        Product product = Product.builder().id(1L).name("Mouse").stock(2).active(true).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        inventoryService.restock(1L, 4);

        assertThat(product.getStock()).isEqualTo(6);
    }
}
