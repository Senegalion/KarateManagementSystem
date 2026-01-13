package com.karate.authservice.infrastructure.client;

import com.karate.authservice.infrastructure.client.config.FeignClientConfig;
import com.karate.authservice.infrastructure.client.dto.NewUserRequestDto;
import com.karate.authservice.infrastructure.client.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserClient {
    @PostMapping("/internal/users")
    Long createUser(@RequestBody NewUserRequestDto newUserRequestDto);

    @GetMapping("/internal/users/{id}")
    UserInfoDto getUserById(@PathVariable("id") Long userId);

    @PutMapping("/internal/users/{userId}/club/{clubId}")
    void updateUserClubId(@PathVariable("userId") Long userId,
                          @PathVariable("clubId") Long clubId);
}
