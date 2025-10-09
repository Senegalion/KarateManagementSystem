package com.karate.payment_service.api.controller.rest;

import com.karate.payment_service.api.dto.*;
import com.karate.payment_service.domain.service.AuthResolver;
import com.karate.payment_service.domain.service.PaymentApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentRESTController {

    private final PaymentApplicationService service;
    private final AuthResolver authResolver;

    @GetMapping("/me/unpaid")
    public UnpaidSummaryDto myUnpaid(Authentication auth) {
        Long userId = authResolver.resolveUserId(auth);
        return service.getUnpaidSummary(userId);
    }

    @GetMapping("/me/history")
    public List<PaymentHistoryItemDto> myHistory(Authentication auth) {
        Long userId = authResolver.resolveUserId(auth);
        return service.history(userId);
    }

    public record CreateOrderMeRequest(
            List<YearMonth> months,
            String currency,
            String returnUrl,
            String cancelUrl
    ) {
    }

    @PostMapping("/me/create-order")
    public CreateOrderResponse createOrder(Authentication auth,
                                           @RequestBody @Valid CreateOrderMeRequest req) {
        Long userId = authResolver.resolveUserId(auth);

        CreateOrderRequest delegate = new CreateOrderRequest(
                userId,
                req.months(),
                req.currency(),
                req.returnUrl(),
                req.cancelUrl()
        );
        return service.createOrder(delegate);
    }

    @PostMapping("/capture/{orderId}")
    public CaptureResponse capture(@PathVariable String orderId) {
        return service.capture(orderId);
    }

    public record ManualPaymentRequest(Long userId, List<YearMonth> months) {
    }

    @PostMapping("/admin/payments/manual")
    public ResponseEntity<Void> manual(@RequestBody ManualPaymentRequest req) {
        service.manualPayment(req.userId(), req.months());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/payments/user/{userId}/unpaid")
    public UnpaidSummaryDto unpaidFor(@PathVariable Long userId) {
        return service.getUnpaidSummary(userId);
    }

    @GetMapping("/admin/payments/user/{userId}/history")
    public List<PaymentHistoryItemDto> historyFor(@PathVariable Long userId) {
        return service.history(userId);
    }
}
