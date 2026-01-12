package com.karate.feedback_service.infrastructure.client;

import com.karate.feedback_service.infrastructure.client.dto.UserInfoDto
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
interface UserClient {
    @GetMapping("/internal/users/{userId}/exists")
    fun checkUserExists(@PathVariable userId: Long): Boolean?

    @GetMapping("/internal/users/{id}")
    fun getUserById(@PathVariable("id") userId: Long): UserInfoDto
}
