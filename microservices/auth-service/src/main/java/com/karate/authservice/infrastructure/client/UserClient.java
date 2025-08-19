package com.karate.authservice.infrastructure.client;

import com.karate.authservice.infrastructure.client.dto.NewUserRequestDto;
import com.karate.authservice.infrastructure.client.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/internal/users")
    Long createUser(@RequestBody NewUserRequestDto newUserRequestDto);

    @GetMapping("/internal/users/{id}")
    UserInfoDto getUserById(@PathVariable("id") Long userId);
}
