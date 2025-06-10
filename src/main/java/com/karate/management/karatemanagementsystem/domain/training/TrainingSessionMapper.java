package com.karate.management.karatemanagementsystem.domain.training;

import com.karate.management.karatemanagementsystem.domain.training.dto.TrainingSessionDto;

public class TrainingSessionMapper {
    public static TrainingSessionDto mapToTrainingSessionDto(TrainingSessionEntity trainingSessionEntity) {
        return TrainingSessionDto.builder()
                .trainingSessionId(trainingSessionEntity.getTrainingSessionId())
                .date(trainingSessionEntity.getDate())
                .description(trainingSessionEntity.getDescription())
                .build();
    }
}
