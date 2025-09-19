package com.karate.training_service.infrastructure.client;

import com.karate.training_service.infrastructure.client.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @GetMapping("/internal/users/{username}/club-id")
    Long getUserClubId(@PathVariable("username") String username);
}
