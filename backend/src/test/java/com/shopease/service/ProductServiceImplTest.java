package com.shopease.service;

import com.shopease.entity.Product;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.mapper.ProductMapper;
import com.shopease.repository.CategoryRepository;
import com.shopease.repository.ProductRepository;
import com.shopease.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductMapper productMapper;
    @InjectMocks ProductServiceImpl productService;

    @Test
    void getById_throwsWhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    void getById_hidesInactiveProducts() {
        Product hidden = Product.builder().id(5L).name("Old mouse").active(false).build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(hidden));

        assertThatThrownBy(() -> productService.getById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
