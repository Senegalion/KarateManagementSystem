package com.karate.enrollment_service.infrastructure.client;

import com.karate.enrollment_service.infrastructure.client.config.FeignClientConfig;
import com.karate.enrollment_service.infrastructure.client.dto.UserInfoDto;
import com.karate.enrollment_service.infrastructure.client.dto.UserPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserClient {
    @GetMapping("/internal/users/{userId}/exists")
    Boolean checkUserExists(@PathVariable Long userId);

    @GetMapping("/internal/users/{id}")
    UserInfoDto getUserById(@PathVariable("id") Long userId);

    @GetMapping("/internal/users/payload/{id}")
    UserPayload getUser(@PathVariable("id") Long userId);
}
