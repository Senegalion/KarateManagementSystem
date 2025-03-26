package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class PayPalClient implements PayPalClientInterface {
    private final PayPalHttpClient payPalHttpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public PayPalClient(PayPalHttpClient payPalHttpClient, ObjectMapper objectMapper) {
        this.payPalHttpClient = payPalHttpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createPayment(String jsonPayload) throws IOException {
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("Content-Type", "application/json");
        request.requestBody(objectMapper.readValue(jsonPayload, Map.class));

        Order order = payPalHttpClient.execute(request).result();
        return order.id();
    }

    @Override
    public boolean confirmPayment(String paymentId) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(String.valueOf(paymentId));
        return payPalHttpClient.execute(request).statusCode() == 200;
    }
}
