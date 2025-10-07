package com.karate.enrollment_service.infrastructure.client;

import com.karate.enrollment_service.infrastructure.client.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", configuration = FeignClientConfig.class)
public interface AuthClient {
    @GetMapping("/internal/users/username/by-id/{username}")
    Long getUserIdByUsername(@PathVariable String username);
}
