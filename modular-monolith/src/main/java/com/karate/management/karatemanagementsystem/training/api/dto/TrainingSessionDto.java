package com.karate.management.karatemanagementsystem.training.api.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TrainingSessionDto(
        Long trainingSessionId,
        LocalDateTime date,
        String description
) {
}
