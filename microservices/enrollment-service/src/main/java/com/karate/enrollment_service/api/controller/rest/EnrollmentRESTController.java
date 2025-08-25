package com.karate.enrollment_service.api.controller.rest;

import com.karate.enrollment_service.api.dto.EnrollmentDto;
import com.karate.enrollment_service.domain.mapper.EnrollmentMapper;
import com.karate.enrollment_service.domain.model.EnrollmentEntity;
import com.karate.enrollment_service.domain.service.EnrollmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/enrollments")
public class EnrollmentRESTController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentMapper enrollmentMapper;

    @PostMapping("/{userId}/{trainingId}")
    public ResponseEntity<EnrollmentDto> enrollUser(
            @PathVariable Long userId,
            @PathVariable Long trainingId) {
        EnrollmentEntity enrollment = enrollmentService.enrollUser(userId, trainingId);
        return ResponseEntity.ok(enrollmentMapper.toDto(enrollment));
    }

    @DeleteMapping("/{userId}/{trainingId}")
    public ResponseEntity<Void> withdrawUser(
            @PathVariable Long userId,
            @PathVariable Long trainingId) {
        enrollmentService.withdrawUser(userId, trainingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDto>> getUserEnrollments(@PathVariable Long userId) {
        return ResponseEntity.ok(
                enrollmentService.getUserEnrollments(userId)
                        .stream()
                        .map(enrollmentMapper::toDto)
                        .toList()
        );
    }

    @GetMapping("/training/{trainingId}")
    public ResponseEntity<List<EnrollmentDto>> getTrainingEnrollments(@PathVariable Long trainingId) {
        return ResponseEntity.ok(
                enrollmentService.getTrainingEnrollments(trainingId)
                        .stream()
                        .map(enrollmentMapper::toDto)
                        .toList()
        );
    }
}
