package com.karate.payment_service.infrastructure.paypal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PayPalClient {

    private final ObjectMapper objectMapper;

    @Value("${paypal.client-id:}")
    private String clientId;

    @Value("${paypal.client-secret:}")
    private String clientSecret;

    @Value("${paypal.mode:sandbox}")
    private String mode;

    private PayPalHttpClient httpClient() {
        PayPalEnvironment env = "live".equalsIgnoreCase(mode)
                ? new PayPalEnvironment.Live(clientId, clientSecret)
                : new PayPalEnvironment.Sandbox(clientId, clientSecret);
        return new PayPalHttpClient(env);
    }

    public String createOrder(Map<String, Object> body) throws IOException {
        OrdersCreateRequest req = new OrdersCreateRequest();
        req.header("Content-Type", "application/json");
        req.requestBody(body);
        Order order = httpClient().execute(req).result();
        return order.links().stream()
                .filter(l -> "approve".equalsIgnoreCase(l.rel()))
                .findFirst()
                .map(LinkDescription::href)
                .map(url -> url.split("token=")[1])
                .orElseThrow(() -> new RuntimeException("PayPal approval URL not found"));
    }

    public boolean captureOrder(String orderId) throws IOException, InterruptedException, JSONException {
        String token = OAuthToken.fetch(clientId, clientSecret, mode);
        String host = "https://api-m." + ("live".equalsIgnoreCase(mode) ? "" : "sandbox.") + "paypal.com";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/v2/checkout/orders/" + orderId + "/capture"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            // status==COMPLETED → zapłacone
            return response.body().contains("\"status\":\"COMPLETED\"");
        }
        return false;
    }
}
