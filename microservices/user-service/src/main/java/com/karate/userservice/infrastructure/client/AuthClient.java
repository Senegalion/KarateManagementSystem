package com.karate.userservice.infrastructure.client;

import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/auth/users/by-ids")
    Map<Long, AuthUserDto> getAuthUsers(@RequestParam("ids") List<Long> userIds);

    @GetMapping("/internal/users/{userId}")
    AuthUserDto getAuthUserByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/by-username/{username}")
    AuthUserDto getAuthUserByUsername(@PathVariable("username") String username);

    @GetMapping("/internal/users/payload/{userId}")
    String getUsernameById(@PathVariable("userId") Long userId);

    @PutMapping("/internal/users/{userId}/username")
    void updateUsername(@PathVariable("userId") Long userId, @RequestBody String newUsername);

    @DeleteMapping("/internal/users/{userId}")
    void deleteUser(@PathVariable("userId") Long userId);
}
