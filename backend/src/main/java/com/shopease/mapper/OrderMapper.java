package com.shopease.mapper;

import com.shopease.dto.response.AddressResponse;
import com.shopease.dto.response.OrderItemResponse;
import com.shopease.dto.response.OrderResponse;
import com.shopease.entity.Address;
import com.shopease.entity.Order;
import com.shopease.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream().map(this::toItemResponse).toList();
        return new OrderResponse(
                o.getId(),
                o.getStatus(),
                o.getTotalAmount(),
                o.getUser().getEmail(),
                toAddressResponse(o.getAddress()),
                items,
                o.getCreatedAt());
    }

    private OrderItemResponse toItemResponse(OrderItem i) {
        return new OrderItemResponse(
                i.getProduct().getId(),
                i.getProductName(),
                i.getUnitPrice(),
                i.getQuantity(),
                i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
    }

    private AddressResponse toAddressResponse(Address a) {
        return new AddressResponse(a.getStreet(), a.getCity(), a.getState(),
                a.getPostalCode(), a.getCountry(), a.getPhone());
    }
}
