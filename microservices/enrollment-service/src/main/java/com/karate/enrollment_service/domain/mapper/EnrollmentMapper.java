package com.karate.enrollment_service.domain.mapper;

import com.karate.enrollment_service.api.dto.EnrollmentDto;
import com.karate.enrollment_service.domain.model.EnrollmentEntity;
import com.karate.enrollment_service.infrastructure.client.TrainingClient;
import com.karate.enrollment_service.infrastructure.client.UserClient;
import com.karate.enrollment_service.infrastructure.client.dto.TrainingSessionDto;
import com.karate.enrollment_service.infrastructure.client.dto.UserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentMapper {

    private final UserClient userClient;
    private final TrainingClient trainingClient;

    public EnrollmentDto toDto(EnrollmentEntity entity) {
        UserInfoDto user = userClient.getUserById(entity.getUserId());
        TrainingSessionDto training = trainingClient.getTrainingById(entity.getTrainingId());

        return EnrollmentDto.builder()
                .enrollmentId(entity.getEnrollmentId())
                .user(user)
                .training(training)
                .enrolledAt(entity.getEnrolledAt())
                .build();
    }
}
