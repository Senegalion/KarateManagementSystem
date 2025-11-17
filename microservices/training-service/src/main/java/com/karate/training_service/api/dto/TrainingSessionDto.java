package com.karate.training_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Training session details.")
public record TrainingSessionDto(
        @Schema(description = "Training session identifier.", example = "33")
        Long trainingSessionId,
        @Schema(description = "Start time.", example = "2025-02-12T15:00:00")
        LocalDateTime startTime,
        @Schema(description = "End time.", example = "2025-02-12T16:30:00")
        LocalDateTime endTime,
        @Schema(description = "Description of training.", example = "Kumite - advanced")
        String description
) {
}
