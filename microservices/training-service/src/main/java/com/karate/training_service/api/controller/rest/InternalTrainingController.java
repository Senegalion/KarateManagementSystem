package com.karate.training_service.api.controller.rest;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.domain.service.TrainingSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/trainings")
public class InternalTrainingController {
    private final TrainingSessionService trainingSessionService;

    public InternalTrainingController(TrainingSessionService trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }

    @GetMapping("/{trainingId}/exists")
    public ResponseEntity<Boolean> checkTrainingExists(@PathVariable Long trainingId) {
        return ResponseEntity.ok(trainingSessionService.checkTrainingExists(trainingId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingSessionDto> getTrainingById(@PathVariable("id") Long trainingId) {
        TrainingSessionDto trainingSessionDto = trainingSessionService.getTrainingById(trainingId);
        return ResponseEntity.ok(trainingSessionDto);
    }
}
