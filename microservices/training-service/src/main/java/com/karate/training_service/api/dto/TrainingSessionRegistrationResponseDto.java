package com.karate.training_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Response returned after successful training registration.")
public record TrainingSessionRegistrationResponseDto(

        @Schema(description = "Message for user.", example = "Successfully enrolled.")
        String message,

        @Schema(description = "Start time.", example = "2025-01-12T15:00:00")
        LocalDateTime startTime,

        @Schema(description = "End time.", example = "2025-01-12T16:30:00")
        LocalDateTime endTime,

        @Schema(description = "Training description.", example = "Basics training")
        String description
) {
}
