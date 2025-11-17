package com.karate.enrollment_service.api.controller.rest;

import com.karate.enrollment_service.api.dto.EnrollmentDto;
import com.karate.enrollment_service.domain.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/enrollments")
@Tag(name = "Enrollments", description = "Enrollment of users into trainings.")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentRESTController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/{userId}/{trainingId}")
    @Operation(summary = "Enroll arbitrary user in training", description = "Admin-level operation.")
    public ResponseEntity<EnrollmentDto> enrollUser(
            @PathVariable Long userId,
            @PathVariable Long trainingId) {
        long t0 = System.currentTimeMillis();
        log.info("POST /enrollments/{}/{}", userId, trainingId);
        EnrollmentDto enrollment = enrollmentService.enrollUser(userId, trainingId);
        log.info("200 /enrollments enrol userId={} trainingId={} took={}ms",
                userId, trainingId, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(enrollment);
    }

    @DeleteMapping("/{userId}/{trainingId}")
    @Operation(summary = "Withdraw arbitrary user from training", description = "Admin-level operation.")
    public ResponseEntity<Void> withdrawUser(
            @PathVariable Long userId,
            @PathVariable Long trainingId) {
        long t0 = System.currentTimeMillis();
        log.info("DELETE /enrollments/{}/{}", userId, trainingId);
        enrollmentService.withdrawUser(userId, trainingId);
        log.info("204 /enrollments withdraw userId={} trainingId={} took={}ms",
                userId, trainingId, System.currentTimeMillis() - t0);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get enrollments for given user")
    public ResponseEntity<List<EnrollmentDto>> getUserEnrollments(@PathVariable Long userId) {
        log.info("GET /enrollments/user/{}", userId);
        return ResponseEntity.ok(
                enrollmentService.getUserEnrollments(userId)
        );
    }

    @GetMapping("/training/{trainingId}")
    @Operation(summary = "Get enrollments for given training")
    public ResponseEntity<List<EnrollmentDto>> getTrainingEnrollments(@PathVariable Long trainingId) {
        log.info("GET /enrollments/training/{}", trainingId);
        return ResponseEntity.ok(
                enrollmentService.getTrainingEnrollments(trainingId)
        );
    }

    @GetMapping("/me")
    @Operation(summary = "Get my enrollments", description = "Uses authenticated user from JWT.")
    public ResponseEntity<List<EnrollmentDto>> getMyEnrollments(Authentication auth) {
        long t0 = System.currentTimeMillis();
        Long userId = enrollmentService.resolveUserId(auth);
        log.info("GET /enrollments/me resolved userId={}", userId);
        var body = enrollmentService.getUserEnrollments(userId);
        log.info("200 /enrollments/me userId={} took={}ms", userId, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/me/{trainingId}")
    @Operation(summary = "Enroll myself into training")
    public ResponseEntity<EnrollmentDto> enrollMe(
            Authentication auth,
            @PathVariable Long trainingId) {
        long t0 = System.currentTimeMillis();
        Long userId = enrollmentService.resolveUserId(auth);
        log.info("POST /enrollments/me/{} userId={}", trainingId, userId);
        var dto = enrollmentService.enrollUser(userId, trainingId);
        log.info("200 /enrollments/me enroll userId={} trainingId={} took={}ms",
                userId, trainingId, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/me/{trainingId}")
    @Operation(summary = "Withdraw myself from training")
    public ResponseEntity<Void> withdrawMe(
            Authentication auth,
            @PathVariable Long trainingId) {
        long t0 = System.currentTimeMillis();
        Long userId = enrollmentService.resolveUserId(auth);
        log.info("DELETE /enrollments/me/{} userId={}", trainingId, userId);
        enrollmentService.withdrawUser(userId, trainingId);
        log.info("204 /enrollments/me withdraw userId={} trainingId={} took={}ms",
                userId, trainingId, System.currentTimeMillis() - t0);
        return ResponseEntity.noContent().build();
    }
}
