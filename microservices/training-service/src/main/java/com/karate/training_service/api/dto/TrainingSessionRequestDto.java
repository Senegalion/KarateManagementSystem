package com.karate.training_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Payload for creating a training session.")
public record TrainingSessionRequestDto(
        @NotNull(message = "{training.startTime.not.null}")
        @Schema(description = "Start time.", example = "2025-02-12T15:00:00")
        LocalDateTime startTime,
        @NotNull(message = "{training.endTime.not.null}")
        @Schema(description = "End time.", example = "2025-02-12T16:30:00")
        LocalDateTime endTime,
        @NotBlank(message = "{training.description.not.blank}")
        @Schema(description = "Description.", example = "Children beginner class")
        String description
) {
}
