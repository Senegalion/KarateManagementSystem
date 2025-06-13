package com.karate.management.karatemanagementsystem.payment.infrastructure.api.paypal.client;

import org.springframework.boot.configurationprocessor.json.JSONException;

import java.io.IOException;

public interface PayPalClientInterface {
    String createPayment(String jsonPayload) throws IOException;

    boolean capturePayment(String orderId) throws IOException, JSONException, InterruptedException;
}
