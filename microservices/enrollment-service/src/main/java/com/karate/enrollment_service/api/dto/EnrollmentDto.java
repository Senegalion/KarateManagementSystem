package com.karate.enrollment_service.api.dto;

import com.karate.enrollment_service.infrastructure.client.dto.TrainingSessionDto;
import com.karate.enrollment_service.infrastructure.client.dto.UserInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Represents a single enrollment of a user to a training session.")
public record EnrollmentDto(
        @Schema(description = "Enrollment identifier.", example = "1001")
        Long enrollmentId,
        @Schema(description = "User details.")
        UserInfoDto user,
        @Schema(description = "Training session details.")
        TrainingSessionDto training,
        @Schema(description = "Timestamp of enrollment.", example = "2025-01-12T12:30:00")
        LocalDateTime enrolledAt
) {
}
