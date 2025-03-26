package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.service;

import com.karate.management.karatemanagementsystem.model.entity.PaymentEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.PaymentRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.model.staticdata.PaymentStatus;
import com.karate.management.karatemanagementsystem.service.exception.PaymentAlreadyConfirmed;
import com.karate.management.karatemanagementsystem.service.exception.PaymentNotFoundException;
import com.karate.management.karatemanagementsystem.service.exception.UserNotFoundException;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {
    private final PayPalHttpClient payPalHttpClient;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${paypal.currency}")
    private String currency;

    public PayPalService(PayPalHttpClient payPalHttpClient, PaymentRepository paymentRepository, UserRepository userRepository) {
        this.payPalHttpClient = payPalHttpClient;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createPayment(Long userId) throws IOException {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        purchaseUnits.add(new PurchaseUnitRequest()
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode(currency)
                        .value(new BigDecimal("150.00").toString()))
        );

        orderRequest.purchaseUnits(purchaseUnits);
        OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);
        Order order = payPalHttpClient.execute(request).result();

        PaymentEntity payment = new PaymentEntity();
        payment.setUserEntity(userEntity);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setPaymentDate(java.time.LocalDate.now());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        userEntity.getPaymentEntities().add(payment);

        userRepository.save(userEntity);
        paymentRepository.save(payment);

        return order.id();
    }

    @Transactional
    public void confirmPayment(String paymentId) {
        PaymentEntity payment = paymentRepository.findById(Long.parseLong(paymentId))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new PaymentAlreadyConfirmed("Payment already confirmed");
        }

        payment.setPaymentStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
    }
}
