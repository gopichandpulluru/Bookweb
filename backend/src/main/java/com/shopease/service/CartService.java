package com.shopease.service;

import com.shopease.dto.request.CartItemRequest;
import com.shopease.dto.response.CartResponse;
import com.shopease.entity.Cart;
import com.shopease.entity.CartItem;
import com.shopease.entity.Product;
import com.shopease.entity.User;
import com.shopease.exception.OutOfStockException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.mapper.CartMapper;
import com.shopease.repository.CartRepository;
import com.shopease.repository.ProductRepository;
import com.shopease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartResponse getCart(String email) {
        return cartMapper.toResponse(getOrCreateCart(email));
    }

    public CartResponse addItem(String email, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        Product product = productRepository.findById(request.productId())
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.productId()));

        CartItem existing = findItem(cart, product.getId());
        int newQuantity = (existing == null ? 0 : existing.getQuantity()) + request.quantity();
        checkStock(product, newQuantity);

        if (existing != null) {
            existing.setQuantity(newQuantity);
        } else {
            cart.getItems().add(CartItem.builder().cart(cart).product(product).quantity(newQuantity).build());
        }
        cartRepository.saveAndFlush(cart);   // flush so new items get their ids before we build the response
        return cartMapper.toResponse(cart);
    }

    public CartResponse updateQuantity(String email, Long productId, int quantity) {
        Cart cart = getOrCreateCart(email);
        CartItem item = findItem(cart, productId);
        if (item == null) {
            throw new ResourceNotFoundException("Cart item", "productId", productId);
        }
        checkStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return cartMapper.toResponse(cart);
    }

    public CartResponse removeItem(String email, Long productId) {
        Cart cart = getOrCreateCart(email);
        boolean removed = cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item", "productId", productId);
        }
        return cartMapper.toResponse(cart);
    }

    public void clear(String email) {
        getOrCreateCart(email).getItems().clear();
    }

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private CartItem findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    // Friendly early check. The real (locked) check happens again when the order is placed.
    private void checkStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            throw new OutOfStockException(product.getName(), quantity, product.getStock());
        }
    }
}
