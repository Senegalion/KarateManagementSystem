package com.karate.payment_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/internal/users/username/by-id/{username}")
    Long getUserIdByUsername(@PathVariable String username);
}
