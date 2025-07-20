package com.karate.feedback_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{userId}/exists")
    Boolean checkUserExists(@PathVariable Long userId);

    @GetMapping("/users/{userId}/training-sessions/{sessionId}/enrolled")
    Boolean checkUserEnrolledInSession(@PathVariable Long userId, @PathVariable Long sessionId);

    @GetMapping("/users/by-username/{username}/id")
    Long getUserIdByUsername(@PathVariable String username);
}
