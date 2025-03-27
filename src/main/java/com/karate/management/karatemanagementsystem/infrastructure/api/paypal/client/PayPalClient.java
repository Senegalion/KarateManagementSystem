package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
public class PayPalClient implements PayPalClientInterface {
    private final PayPalHttpClient payPalHttpClient;
    private final ObjectMapper objectMapper;

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

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

        return order.links().stream()
                .filter(link -> "approve".equalsIgnoreCase(link.rel()))
                .findFirst()
                .map(LinkDescription::href)
                .map(url -> url.split("token=")[1])
                .orElseThrow(() -> new RuntimeException("Approval URL not found"));
    }

    @Override
    public boolean capturePayment(String orderId) throws IOException, InterruptedException, JSONException {
        String accessToken = getAccessToken();
        String url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderId + "/capture";

        ObjectNode requestBody = JsonNodeFactory.instance.objectNode();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("PayPal-Request-Id", generateUniqueRequestId())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("PayPal response: " + response.body());

        if (response.statusCode() == 201) {
            JSONObject jsonResponse = new JSONObject(response.body());
            String status = jsonResponse.getString("status");

            return "COMPLETED".equals(status);
        }
        return false;
    }


    private String getAccessToken() throws IOException, InterruptedException, JSONException {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("PayPal response: " + response.body());
        if (response.statusCode() == 200) {
            return new JSONObject(response.body()).getString("access_token");
        }

        throw new RuntimeException("Failed to get access token");
    }

    private String generateUniqueRequestId() {
        return "REQ-" + UUID.randomUUID();
    }
}
