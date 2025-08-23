package com.karate.feedback_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
interface UserClient {
    @GetMapping("/users/{userId}/exists")
    fun checkUserExists(@PathVariable userId: Long): Boolean?

    @GetMapping("/users/{userId}/training-sessions/{sessionId}/enrolled")
    fun checkUserEnrolledInSession(
            @PathVariable userId: Long,
            @PathVariable sessionId: Long
    ): Boolean?

    @GetMapping("/users/by-username/{username}/id")
    fun getUserIdByUsername(@PathVariable username: String): Long?
}
