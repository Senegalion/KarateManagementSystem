package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.controller;

import com.karate.management.karatemanagementsystem.infrastructure.api.paypal.service.PayPalService;
import com.karate.management.karatemanagementsystem.model.dto.paypal.PaymentRequestDto;
import com.karate.management.karatemanagementsystem.model.dto.paypal.PaymentResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/paypal")
public class PayPalRESTController {
    private final PayPalService payPalService;

    @PostMapping("/create-payment")
    public ResponseEntity<PaymentResponseDto> createPayment(@RequestBody PaymentRequestDto paymentRequest) {
        PaymentResponseDto paymentResponseDto = payPalService.createPayment(paymentRequest);
        return new ResponseEntity<>(paymentResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/capture-payment/{paymentId}")
    public ResponseEntity<PaymentResponseDto> capturePayment(@PathVariable String paymentId) throws JSONException {
        PaymentResponseDto paymentResponseDto = payPalService.capturePayment(paymentId);
        return new ResponseEntity<>(paymentResponseDto, HttpStatus.CREATED);
    }
}
