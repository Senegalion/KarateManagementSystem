package com.karate.enrollment_service.api.dto;

import com.karate.enrollment_service.infrastructure.client.dto.TrainingSessionDto;
import com.karate.enrollment_service.infrastructure.client.dto.UserInfoDto;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EnrollmentDto(
        Long enrollmentId,
        UserInfoDto user,
        TrainingSessionDto training,
        LocalDateTime enrolledAt
) {
}
