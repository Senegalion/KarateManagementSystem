package com.karate.management.karatemanagementsystem.training.infrastructure.persistence.mapper;

import com.karate.management.karatemanagementsystem.training.domain.model.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.training.api.dto.TrainingSessionDto;

public class TrainingSessionMapper {
    public static TrainingSessionDto mapToTrainingSessionDto(TrainingSessionEntity trainingSessionEntity) {
        return TrainingSessionDto.builder()
                .trainingSessionId(trainingSessionEntity.getTrainingSessionId())
                .date(trainingSessionEntity.getDate())
                .description(trainingSessionEntity.getDescription())
                .build();
    }
}
