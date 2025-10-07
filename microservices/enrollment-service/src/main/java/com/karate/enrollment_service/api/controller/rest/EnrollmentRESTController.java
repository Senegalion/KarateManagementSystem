package com.karate.enrollment_service.api.controller.rest;

import com.karate.enrollment_service.api.dto.EnrollmentDto;
import com.karate.enrollment_service.domain.service.EnrollmentService;
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
public class EnrollmentRESTController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/{userId}/{trainingId}")
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
    public ResponseEntity<List<EnrollmentDto>> getUserEnrollments(@PathVariable Long userId) {
        log.info("GET /enrollments/user/{}", userId);
        return ResponseEntity.ok(
                enrollmentService.getUserEnrollments(userId)
        );
    }

    @GetMapping("/training/{trainingId}")
    public ResponseEntity<List<EnrollmentDto>> getTrainingEnrollments(@PathVariable Long trainingId) {
        log.info("GET /enrollments/training/{}", trainingId);
        return ResponseEntity.ok(
                enrollmentService.getTrainingEnrollments(trainingId)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<EnrollmentDto>> getMyEnrollments(Authentication auth) {
        long t0 = System.currentTimeMillis();
        Long userId = enrollmentService.resolveUserId(auth);
        log.info("GET /enrollments/me resolved userId={}", userId);
        var body = enrollmentService.getUserEnrollments(userId);
        log.info("200 /enrollments/me userId={} took={}ms", userId, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/me/{trainingId}")
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
