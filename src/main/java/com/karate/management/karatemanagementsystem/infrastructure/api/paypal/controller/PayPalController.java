package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.controller;

import com.karate.management.karatemanagementsystem.infrastructure.api.paypal.service.PayPalService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/paypal")
public class PayPalController {
    private final PayPalService payPalService;

    @GetMapping("/create-payment")
    public String createPayment(@RequestParam Long userId) throws IOException {
        return payPalService.createPayment(userId);
    }

    @GetMapping("/confirm-payment")
    public void confirmPayment(@RequestParam String paymentId) {
        payPalService.confirmPayment(paymentId);
    }
}
