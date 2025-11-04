package com.karate.payment_service.domain.service;

import com.karate.payment_service.api.dto.*;
import com.karate.payment_service.domain.model.PaymentEntity;
import com.karate.payment_service.domain.model.PaymentItemEntity;
import com.karate.payment_service.domain.model.PaymentProvider;
import com.karate.payment_service.domain.model.PaymentStatus;
import com.karate.payment_service.domain.repository.PaymentItemRepository;
import com.karate.payment_service.domain.repository.PaymentRepository;
import com.karate.payment_service.domain.repository.UserAccountRepository;
import com.karate.payment_service.infrastructure.messaging.PaymentEventPublisher;
import com.karate.payment_service.infrastructure.messaging.dto.PaymentReceivedEvent;
import com.karate.payment_service.infrastructure.paypal.PayPalClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentsConfig cfg;
    private final UserAccountRepository users;
    private final PaymentRepository payments;
    private final PaymentItemRepository items;
    private final UnpaidCalculator unpaidCalculator;
    private final PayPalClient payPal;
    private final PaymentEventPublisher publisher;

    @Transactional(readOnly = true)
    public UnpaidSummaryDto getUnpaidSummary(Long userId) {
        var u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        var months = unpaidCalculator.unpaidMonths(u).stream().map(YearMonth::toString).toList();
        var total = cfg.getMonthlyFee().multiply(BigDecimal.valueOf(months.size()));
        return new UnpaidSummaryDto(months, cfg.getMonthlyFee(), total);
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryItemDto> history(Long userId) {
        return payments.findByUserIdOrderByCreatedAtDesc(userId).stream().map(p -> {
            var m = p.getItems().stream().map(i -> i.getYearMonth().toString()).toList();
            return new PaymentHistoryItemDto(
                    p.getPaymentId(), p.getProvider().name(), p.getProviderOrderId(), p.getCurrency(),
                    p.getAmount(), p.getStatus(), p.getCreatedAt(), p.getPaidAt(), m
            );
        }).toList();
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest req) {
        var u = users.findById(req.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.userId()));

        var months = req.months();
        if (months == null || months.isEmpty()) {
            months = List.of(YearMonth.now());
        }

        for (YearMonth ym : months) {
            if (items.existsByUserIdAndYearMonthAndStatus(u.getUserId(), ym, PaymentStatus.PAID)) {
                throw new IllegalStateException("Month already paid: " + ym);
            }
        }

        BigDecimal amount = cfg.getMonthlyFee().multiply(BigDecimal.valueOf(months.size()));
        String currency = Optional.ofNullable(req.currency()).orElse(cfg.getCurrency());

        // PayPal payload
        Map<String, Object> amountMap = Map.of("currency_code", currency, "value", amount.toPlainString());
        Map<String, Object> purchaseUnit = Map.of("amount", amountMap);
        Map<String, Object> appCtx = Map.of(
                "return_url", Optional.ofNullable(req.returnUrl()).orElse("https://example.com/success"),
                "cancel_url", Optional.ofNullable(req.cancelUrl()).orElse("https://example.com/cancel"),
                "brand_name", "Karate Management",
                "landing_page", "LOGIN",
                "user_action", "PAY_NOW"
        );
        Map<String, Object> body = new HashMap<>();
        body.put("intent", "CAPTURE");
        body.put("purchase_units", List.of(purchaseUnit));
        body.put("application_context", appCtx);

        try {
            String orderId = payPal.createOrder(body); // token z URL (order id)

            var pay = PaymentEntity.builder()
                    .userId(u.getUserId())
                    .provider(PaymentProvider.PAYPAL)
                    .providerOrderId(orderId)
                    .currency(currency)
                    .amount(amount)
                    .status(PaymentStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();

            var list = months.stream().map(ym -> PaymentItemEntity.builder()
                    .payment(pay)
                    .userId(u.getUserId())
                    .yearMonth(ym)
                    .amount(cfg.getMonthlyFee())
                    .status(PaymentStatus.PENDING)
                    .build()).toList();

            pay.setItems(list);
            payments.save(pay);

            String approvalUrl = "https://www.sandbox.paypal.com/checkoutnow?token=" + orderId;
            return new CreateOrderResponse(orderId, approvalUrl, amount, currency,
                    months.stream().map(YearMonth::toString).toList(), PaymentStatus.PENDING);

        } catch (IOException e) {
            throw new RuntimeException("PayPal create error", e);
        }
    }

    @Transactional
    public CaptureResponse capture(String orderId) {
        var payment = payments.lockByProviderOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + orderId));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return new CaptureResponse(orderId, "ALREADY_PAID");
        }

        try {
            boolean ok = payPal.captureOrder(orderId);
            if (!ok) throw new RuntimeException("PayPal capture rejected");

            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(Instant.now());
            for (var it : payment.getItems()) it.setStatus(PaymentStatus.PAID);
            payments.save(payment);

            var months = payment.getItems().stream().map(i -> i.getYearMonth().toString()).toList();
            publisher.publishReceived(PaymentReceivedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PAYMENT_RECEIVED")
                    .timestamp(Instant.now())
                    .userId(payment.getUserId())
                    .currency(payment.getCurrency())
                    .amount(payment.getAmount())
                    .months(months)
                    .build());

            return new CaptureResponse(orderId, "PAID");
        } catch (IOException | InterruptedException | JSONException e) {
            throw new RuntimeException("PayPal capture error", e);
        }
    }

    @Transactional
    public void manualPayment(Long userId, List<YearMonth> months) {
        var u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        for (YearMonth ym : months) {
            if (items.existsByUserIdAndYearMonthAndStatus(userId, ym, PaymentStatus.PAID)) {
                throw new IllegalStateException("Month already paid: " + ym);
            }
        }

        var amount = cfg.getMonthlyFee().multiply(BigDecimal.valueOf(months.size()));
        var pay = PaymentEntity.builder()
                .userId(userId)
                .provider(PaymentProvider.MANUAL)
                .providerOrderId(null)
                .currency(cfg.getCurrency())
                .amount(amount)
                .status(PaymentStatus.PAID)
                .createdAt(Instant.now())
                .paidAt(Instant.now())
                .build();

        var list = months.stream().map(ym -> PaymentItemEntity.builder()
                .payment(pay)
                .userId(userId)
                .yearMonth(ym)
                .amount(cfg.getMonthlyFee())
                .status(PaymentStatus.PAID)
                .build()).toList();

        pay.setItems(list);
        payments.save(pay);

        publisher.publishReceived(PaymentReceivedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PAYMENT_RECEIVED")
                .timestamp(Instant.now())
                .userId(userId)
                .currency(cfg.getCurrency())
                .amount(amount)
                .months(months.stream().map(YearMonth::toString).toList())
                .build());
    }
}
