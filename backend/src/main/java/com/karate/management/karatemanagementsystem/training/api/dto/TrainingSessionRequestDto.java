package com.karate.management.karatemanagementsystem.training.api.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TrainingSessionRequestDto(
        LocalDateTime date,
        String description
) {
}
