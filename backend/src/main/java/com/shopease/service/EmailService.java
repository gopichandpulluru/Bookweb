package com.shopease.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sends emails in a background thread (@Async) so the API response isn't delayed.
 * A failed email is logged but never breaks the order.
 * Note: pass simple values (not entities) into @Async methods - the database session is gone by then.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    @Async
    public void sendOrderConfirmation(String to, String customerName, Long orderId, BigDecimal total) {
        Context context = new Context();
        context.setVariable("name", customerName);
        context.setVariable("orderId", orderId);
        context.setVariable("total", total);

        String html = templateEngine.process("email/order-confirmation", context);
        send(to, "Your ShopEase order #" + orderId, html);
    }

    @Async
    public void sendLowStockAlert(String to, List<String> lines) {
        String body = "<p>These products are running low:</p><ul>"
                + String.join("", lines.stream().map(l -> "<li>" + l + "</li>").toList())
                + "</ul>";
        send(to, "ShopEase: low stock alert", body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.debug("Sent email '{}' to {}", subject, to);
        } catch (MessagingException | MailException ex) {
            log.warn("Could not send email '{}' to {}: {}", subject, to, ex.getMessage());
        }
    }
}
