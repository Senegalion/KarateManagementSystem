package com.karate.enrollment_service.api.controller.rest;

import com.karate.enrollment_service.domain.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/internal/enrollments")
@Tag(name = "Internal - Enrollments", description = "Internal API used by other microservices.")
public class InternalEnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/enrolled/{userId}/{sessionId}")
    @Operation(summary = "Check if user is enrolled in training (internal)")
    public ResponseEntity<Boolean> checkUserEnrolled(
            @PathVariable Long userId,
            @PathVariable Long sessionId
    ) {
        log.debug("GET /internal/enrollments/enrolled/{}/{}", userId, sessionId);
        boolean enrolled = enrollmentService.isUserEnrolledInSession(userId, sessionId);
        return ResponseEntity.ok(enrolled);
    }
}
