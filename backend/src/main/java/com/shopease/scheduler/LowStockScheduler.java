package com.shopease.scheduler;

import com.shopease.entity.Product;
import com.shopease.service.EmailService;
import com.shopease.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/** Every morning, emails the admin a list of products that are almost sold out. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockScheduler {

    private final InventoryService inventoryService;
    private final EmailService emailService;

    @Value("${app.inventory.low-stock-threshold}")
    private int threshold;

    @Value("${app.admin.alert-email}")
    private String alertEmail;

    @Scheduled(cron = "0 0 8 * * *")   // 08:00 every day (server time)
    public void reportLowStock() {
        List<Product> low = inventoryService.findLowStock(threshold);
        if (low.isEmpty()) {
            return;
        }
        List<String> lines = low.stream()
                .map(p -> p.getName() + " - " + p.getStock() + " left")
                .toList();
        log.info("{} products at or below {} units", low.size(), threshold);
        emailService.sendLowStockAlert(alertEmail, lines);
    }
}
