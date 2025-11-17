package com.karate.payment_service.api.controller.rest;

import com.karate.payment_service.api.dto.*;
import com.karate.payment_service.domain.service.AuthResolver;
import com.karate.payment_service.domain.service.PaymentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "Payments and billing operations.")
@SecurityRequirement(name = "bearerAuth")
public class PaymentRESTController {

    private final PaymentApplicationService service;
    private final AuthResolver authResolver;

    @GetMapping("/me/unpaid")
    @Operation(summary = "Get my unpaid months summary")
    public UnpaidSummaryDto myUnpaid(Authentication auth) {
        Long userId = authResolver.resolveUserId(auth);
        return service.getUnpaidSummary(userId);
    }

    @GetMapping("/me/history")
    @Operation(summary = "Get my payment history")
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
    @Operation(summary = "Create PayPal order for my unpaid months")
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
    @Operation(summary = "Capture PayPal order")
    public CaptureResponse capture(@PathVariable String orderId) {
        return service.capture(orderId);
    }

    public record ManualPaymentRequest(Long userId, List<YearMonth> months) {
    }

    @PostMapping("/admin/payments/manual")
    @Operation(summary = "Register manual payment for user (admin)")
    public ResponseEntity<Void> manual(@RequestBody ManualPaymentRequest req) {
        service.manualPayment(req.userId(), req.months());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/payments/user/{userId}/unpaid")
    @Operation(summary = "Get unpaid summary for given user (admin)")
    public UnpaidSummaryDto unpaidFor(@PathVariable Long userId) {
        return service.getUnpaidSummary(userId);
    }

    @GetMapping("/admin/payments/user/{userId}/history")
    @Operation(summary = "Get payment history for given user (admin)")
    public List<PaymentHistoryItemDto> historyFor(@PathVariable Long userId) {
        return service.history(userId);
    }
}
