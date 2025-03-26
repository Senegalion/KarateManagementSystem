package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.client;

import java.io.IOException;

public interface PayPalClientInterface {
    String createPayment(String jsonPayload) throws IOException;

    boolean confirmPayment(String paymentId) throws IOException;
}
