package com.karate.feedback_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "enrollment-service")
interface EnrollmentClient {
    @GetMapping("/internal/enrollments/enrolled/{userId}/{sessionId}")
    fun checkUserEnrolledInSession(
        @PathVariable userId: Long,
        @PathVariable sessionId: Long
    ): Boolean?
}
