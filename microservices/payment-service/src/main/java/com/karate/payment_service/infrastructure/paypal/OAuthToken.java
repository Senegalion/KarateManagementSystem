package com.karate.payment_service.infrastructure.paypal;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

class OAuthToken {
    static String fetch(String clientId, String clientSecret, String mode) throws IOException, InterruptedException, JSONException {
        String host = "https://api-m." + ("live".equalsIgnoreCase(mode) ? "" : "sandbox.") + "paypal.com";
        String auth = clientId + ":" + clientSecret;
        String basic = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String body = "grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(host + "/v1/oauth2/token"))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 200) {
            return new JSONObject(res.body()).getString("access_token");
        }
        throw new RuntimeException("PayPal OAuth failed: " + res.statusCode() + " body=" + res.body());
    }
}
