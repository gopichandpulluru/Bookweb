package com.shopease.service;

import com.shopease.dto.response.PaymentIntentResponse;
import com.shopease.entity.Order;
import com.shopease.entity.Payment;
import com.shopease.enums.OrderStatus;
import com.shopease.enums.PaymentStatus;
import com.shopease.exception.BadRequestException;
import com.shopease.exception.PaymentException;
import com.shopease.exception.ResourceNotFoundException;
import com.shopease.repository.OrderRepository;
import com.shopease.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.stripe.currency}")
    private String currency;

    /** Step 1 of paying: ask Stripe for a PaymentIntent and hand its clientSecret to the React app. */
    public PaymentIntentResponse createPaymentIntent(String email, Long orderId) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be paid");
        }
        Payment existing = paymentRepository.findByOrderId(orderId).orElse(null);
        if (existing != null && existing.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new BadRequestException("This order has already been paid");
        }

        // Stripe wants the smallest currency unit (paise/cents): 499.50 -> 49950
        long amountInMinorUnits = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        PaymentIntent intent;
        try {
            intent = PaymentIntent.create(PaymentIntentCreateParams.builder()
                    .setAmount(amountInMinorUnits)
                    .setCurrency(currency)
                    .putMetadata("orderId", String.valueOf(order.getId()))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .build());
        } catch (StripeException ex) {
            throw new PaymentException("Could not create Stripe payment intent", ex);
        }

        Payment payment = (existing != null) ? existing : Payment.builder().order(order).build();
        payment.setProviderRef(intent.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return new PaymentIntentResponse(intent.getClientSecret(), intent.getId(),
                order.getTotalAmount(), currency);
    }

    /**
     * Step 2: Stripe calls US (server to server) when the payment finishes.
     * We never trust the browser to say "I paid" - only this signed webhook can mark an order as PAID.
     */
    public void handleWebhook(String payload, String signatureHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException ex) {
            throw new BadRequestException("Invalid Stripe signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> updateFromEvent(event, PaymentStatus.SUCCEEDED);
            case "payment_intent.payment_failed" -> updateFromEvent(event, PaymentStatus.FAILED);
            default -> log.debug("Ignoring Stripe event {}", event.getType());
        }
    }

    private void updateFromEvent(Event event, PaymentStatus newStatus) {
        // Empty if the Stripe account API version differs from the SDK's - keep them aligned in the dashboard
        StripeObject object = event.getDataObjectDeserializer().getObject().orElse(null);
        if (!(object instanceof PaymentIntent intent)) {
            log.warn("Could not read PaymentIntent from Stripe event {}", event.getId());
            return;
        }

        Payment payment = paymentRepository.findByProviderRef(intent.getId()).orElse(null);
        if (payment == null) {
            log.warn("Stripe event for unknown PaymentIntent {}", intent.getId());
            return;
        }
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return;   // Stripe can send the same event more than once - ignore repeats
        }

        payment.setStatus(newStatus);
        Order order = payment.getOrder();
        if (newStatus == PaymentStatus.SUCCEEDED) {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PAID);
            } else {
                log.warn("Payment succeeded for order {} which is {} - needs a manual refund/review",
                        order.getId(), order.getStatus());
            }
        }
    }
}
