package com.karate.feedback_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
interface AuthClient {
    @GetMapping("/internal/users/username/by-id/{username}")
    fun getUserIdByUsername(@PathVariable username: String): Long?
}
