package com.karate.userservice.infrastructure.client;

import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/auth/users/by-ids")
    Map<Long, AuthUserDto> getAuthUsers(@RequestParam("ids") List<Long> userIds);

    @GetMapping("/auth/users/{userId}")
    AuthUserDto getAuthUserByUserId(@PathVariable("userId") Long userId);
}
