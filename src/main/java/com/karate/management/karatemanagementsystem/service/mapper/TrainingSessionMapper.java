package com.karate.management.karatemanagementsystem.service.mapper;

import com.karate.management.karatemanagementsystem.model.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;

public class TrainingSessionMapper {
    public static TrainingSessionDto mapToTrainingSessionDto(TrainingSessionEntity trainingSessionEntity) {
        return TrainingSessionDto.builder()
                .trainingSessionId(trainingSessionEntity.getTrainingSessionId())
                .date(trainingSessionEntity.getDate())
                .description(trainingSessionEntity.getDescription())
                .build();
    }
}
