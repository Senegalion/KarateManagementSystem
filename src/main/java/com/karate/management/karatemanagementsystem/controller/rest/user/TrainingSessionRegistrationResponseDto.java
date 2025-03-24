package com.karate.management.karatemanagementsystem.controller.rest.user;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TrainingSessionRegistrationResponseDto(
        String message,
        LocalDateTime date,
        String description
) {
}
