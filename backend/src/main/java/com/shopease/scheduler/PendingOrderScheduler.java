package com.shopease.scheduler;

import com.shopease.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Unpaid orders hold stock. After 24 hours we cancel them so the items can be sold again. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingOrderScheduler {

    private static final int EXPIRY_HOURS = 24;

    private final OrderService orderService;

    @Scheduled(fixedRate = 60 * 60 * 1000)   // every hour
    public void cancelExpiredOrders() {
        int cancelled = orderService.cancelUnpaidOrdersOlderThan(EXPIRY_HOURS);
        if (cancelled > 0) {
            log.info("Cancelled {} unpaid orders older than {}h", cancelled, EXPIRY_HOURS);
        }
    }
}
