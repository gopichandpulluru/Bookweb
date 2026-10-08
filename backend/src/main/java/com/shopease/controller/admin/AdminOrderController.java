package com.shopease.controller.admin;

import com.shopease.dto.request.UpdateOrderStatusRequest;
import com.shopease.dto.response.OrderResponse;
import com.shopease.dto.response.PageResponse;
import com.shopease.enums.OrderStatus;
import com.shopease.service.OrderService;
import com.shopease.util.Constants;
import com.shopease.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // GET /api/v1/admin/orders?status=PAID&page=0&size=20
    @GetMapping
    public PageResponse<OrderResponse> list(@RequestParam(required = false) OrderStatus status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        return orderService.getAllOrders(status,
                PageUtil.of(page, size, "createdAt", "desc", Set.of("createdAt"), "createdAt"));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }
}
