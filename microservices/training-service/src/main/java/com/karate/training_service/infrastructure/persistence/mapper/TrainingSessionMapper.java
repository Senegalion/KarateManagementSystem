package com.karate.training_service.infrastructure.persistence.mapper;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.domain.model.TrainingSessionEntity;

public class TrainingSessionMapper {

    private TrainingSessionMapper() {
    }

    public static TrainingSessionDto mapToTrainingSessionDto(TrainingSessionEntity trainingSessionEntity) {
        return TrainingSessionDto.builder()
                .trainingSessionId(trainingSessionEntity.getTrainingSessionId())
                .startTime(trainingSessionEntity.getStartTime())
                .endTime(trainingSessionEntity.getEndTime())
                .description(trainingSessionEntity.getDescription())
                .build();
    }
}
