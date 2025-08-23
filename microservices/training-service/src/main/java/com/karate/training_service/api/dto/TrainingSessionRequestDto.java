package com.karate.training_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TrainingSessionRequestDto(
        @NotNull(message = "{training.startTime.not.null}")
        LocalDateTime startTime,
        @NotNull(message = "{training.endTime.not.null}")
        LocalDateTime endTime,
        @NotBlank(message = "{training.description.not.blank}")
        String description
) {
}
