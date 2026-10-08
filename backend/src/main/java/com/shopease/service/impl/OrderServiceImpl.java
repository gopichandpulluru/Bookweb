package com.shopease.service.impl;

import com.shopease.dto.request.AddressRequest;
import com.shopease.dto.request.PlaceOrderRequest;
import com.shopease.dto.response.OrderResponse;
import com.shopease.dto.response.PageResponse;
import com.shopease.entity.*;
import com.shopease.enums.OrderStatus;
import com.shopease.exception.BadRequestException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.mapper.OrderMapper;
import com.shopease.repository.CartRepository;
import com.shopease.repository.OrderRepository;
import com.shopease.repository.UserRepository;
import com.shopease.service.EmailService;
import com.shopease.service.InventoryService;
import com.shopease.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final EmailService emailService;
    private final OrderMapper orderMapper;

    // ------------------------------------------------------------------ customer

    /**
     * ONE transaction: if any step fails (out of stock, DB error...) everything is rolled back,
     * so we never end up with a half-created order or stock that was taken but not ordered.
     */
    @Override
    public OrderResponse placeOrder(String email, PlaceOrderRequest request) {
        User user = findUser(email);

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .address(toAddress(user, request.shippingAddress()))
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // Lock products in id order so two customers ordering the same items can't deadlock each other
        List<CartItem> cartItems = cart.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getId()))
                .toList();

        for (CartItem cartItem : cartItems) {
            Product product = inventoryService.reserve(cartItem.getProduct().getId(), cartItem.getQuantity());

            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())     // snapshot
                    .unitPrice(product.getPrice())      // snapshot
                    .quantity(cartItem.getQuantity())
                    .build());

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        cart.getItems().clear();   // the cart has been "checked out"

        emailService.sendOrderConfirmation(user.getEmail(), user.getFullName(), saved.getId(), total);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(String email, Pageable pageable) {
        return PageResponse.from(orderRepository.findByUserEmail(email, pageable).map(orderMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String email, Long orderId) {
        return orderMapper.toResponse(findOwnOrder(email, orderId));
    }

    @Override
    public OrderResponse cancelMyOrder(String email, Long orderId) {
        Order order = findOwnOrder(email, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only orders that are still PENDING can be cancelled");
        }
        cancelAndRestock(order);
        return orderMapper.toResponse(order);
    }

    // ------------------------------------------------------------------ admin

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        var page = (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PageResponse.from(page.map(orderMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return orderMapper.toResponse(findOrder(orderId));
    }

    @Override
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = findOrder(orderId);
        OrderStatus current = order.getStatus();

        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw new BadRequestException("A " + current + " order can no longer be changed");
        }
        if (newStatus == OrderStatus.CANCELLED) {
            cancelAndRestock(order);
        } else {
            order.setStatus(newStatus);
        }
        return orderMapper.toResponse(order);
    }

    // ------------------------------------------------------------------ scheduler

    @Override
    public int cancelUnpaidOrdersOlderThan(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        List<Order> stale = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);
        stale.forEach(this::cancelAndRestock);
        return stale.size();
    }

    // ------------------------------------------------------------------ helpers

    private void cancelAndRestock(Order order) {
        for (OrderItem item : order.getItems()) {
            inventoryService.restock(item.getProduct().getId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
    }

    private Address toAddress(User user, AddressRequest r) {
        return Address.builder()
                .user(user)
                .street(r.street())
                .city(r.city())
                .state(r.state())
                .postalCode(r.postalCode())
                .country(r.country())
                .phone(r.phone())
                .build();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    // Looking up by id AND owner means a customer can never read someone else's order (gets a 404)
    private Order findOwnOrder(String email, Long id) {
        return orderRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }
}
