package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.karate.management.karatemanagementsystem.infrastructure.api.paypal.client.PayPalClientInterface;
import com.karate.management.karatemanagementsystem.model.dto.paypal.PaymentRequestDto;
import com.karate.management.karatemanagementsystem.model.dto.paypal.PaymentResponseDto;
import com.karate.management.karatemanagementsystem.model.entity.PaymentEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.PaymentRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.model.staticdata.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PayPalService {
    private final PayPalClientInterface payPalClient;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${paypal.currency}")
    private String currency;

    @Value("${paypal.amount}")
    private BigDecimal amount;

    public PayPalService(PayPalClientInterface payPalClient, PaymentRepository paymentRepository,
                         UserRepository userRepository, ObjectMapper objectMapper) {
        this.payPalClient = payPalClient;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto paymentRequest) {
        UserEntity userEntity = getAuthenticatedUser();

        if (paymentRequest.value() == null) {
            paymentRequest = new PaymentRequestDto(
                    userEntity.getUserId(),
                    paymentRequest.currencyCode() != null ? paymentRequest.currencyCode() : currency,
                    amount,
                    paymentRequest.returnUrl(),
                    paymentRequest.cancelUrl()
            );
        }

        if (paymentRequest.value() == null) {
            throw new IllegalArgumentException("Payment value cannot be null.");
        }

        try {
            ObjectNode requestBody = JsonNodeFactory.instance.objectNode();
            requestBody.put("intent", "CAPTURE");

            ObjectNode purchaseUnit = JsonNodeFactory.instance.objectNode();
            ObjectNode amountNode = purchaseUnit.putObject("amount");
            amountNode.put("currency_code", paymentRequest.currencyCode());
            amountNode.put("value", paymentRequest.value().toPlainString());

            requestBody.set("purchase_units", JsonNodeFactory.instance.arrayNode().add(purchaseUnit));

            ObjectNode applicationContext = JsonNodeFactory.instance.objectNode();
            applicationContext.put("return_url", paymentRequest.returnUrl());
            applicationContext.put("cancel_url", paymentRequest.cancelUrl());
            applicationContext.put("brand_name", "Karate Management");
            applicationContext.put("landing_page", "LOGIN");
            applicationContext.put("user_action", "PAY_NOW");

            requestBody.set("application_context", applicationContext);

            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            System.out.println("Generated JSON: " + jsonPayload);

            String token = payPalClient.createPayment(jsonPayload);

            String approvalUrl = "https://www.sandbox.paypal.com/checkoutnow?token=" + token;

            PaymentEntity payment = new PaymentEntity();
            payment.setPaypalOrderId(token);
            payment.setUserEntity(userEntity);
            payment.setAmount(paymentRequest.value());
            payment.setPaymentDate(LocalDate.now());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            userEntity.getPaymentEntities().add(payment);

            userRepository.save(userEntity);
            paymentRepository.save(payment);

            return buildPaymentResponse(token, userEntity.getUserId(), payment, approvalUrl);

        } catch (IOException e) {
            throw new RuntimeException("Error creating payment with PayPal: " + e.getMessage(), e);
        }
    }


    private PaymentResponseDto buildPaymentResponse(String paymentId, Long userId, PaymentEntity payment, String approvalUrl) {
        return PaymentResponseDto.builder()
                .paymentId(paymentId)
                .userId(userId)
                .amount(payment.getAmount())
                .status(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .approvalUrl(approvalUrl)
                .build();
    }

    private PaymentResponseDto buildPaymentResponse(String paymentId, Long userId, PaymentEntity payment) {
        return buildPaymentResponse(paymentId, userId, payment, null);
    }

    @Transactional
    public PaymentResponseDto capturePayment(String paypalOrderId) throws JSONException {
        try {
            PaymentEntity payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                throw new RuntimeException("Payment already confirmed.");
            }

            boolean paymentCaptured = payPalClient.capturePayment(paypalOrderId);
            if (!paymentCaptured) {
                throw new RuntimeException("Payment capture failed.");
            }

            payment.setPaymentStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            return buildPaymentResponse(paypalOrderId, payment.getUserEntity().getUserId(), payment);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error capturing payment: " + e.getMessage(), e);
        }
    }


}
