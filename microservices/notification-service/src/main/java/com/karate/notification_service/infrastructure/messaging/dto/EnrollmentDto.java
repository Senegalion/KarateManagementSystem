package com.karate.notification_service.infrastructure.messaging.dto;

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

