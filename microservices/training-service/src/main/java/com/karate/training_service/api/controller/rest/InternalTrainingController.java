package com.karate.training_service.api.controller.rest;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.domain.service.TrainingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/trainings")
@AllArgsConstructor
@Tag(name = "Internal - Trainings", description = "Internal endpoints for other services (e.g. enrollment).")
public class InternalTrainingController {
    private final TrainingSessionService trainingSessionService;

    @GetMapping("/{trainingId}/exists")
    @Operation(summary = "Check if training exists (internal)")
    public ResponseEntity<Boolean> checkTrainingExists(@PathVariable Long trainingId) {
        log.debug("GET /internal/trainings/{}/exists", trainingId);
        return ResponseEntity.ok(trainingSessionService.checkTrainingExists(trainingId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get training details by id (internal)")
    public ResponseEntity<TrainingSessionDto> getTrainingById(@PathVariable("id") Long trainingId) {
        log.debug("GET /internal/trainings/{}", trainingId);
        return ResponseEntity.ok(trainingSessionService.getTrainingById(trainingId));
    }
}
