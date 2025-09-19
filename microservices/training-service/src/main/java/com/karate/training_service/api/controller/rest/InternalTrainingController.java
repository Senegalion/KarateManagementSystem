package com.karate.training_service.api.controller.rest;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.domain.service.TrainingSessionService;
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
public class InternalTrainingController {
    private final TrainingSessionService trainingSessionService;

    @GetMapping("/{trainingId}/exists")
    public ResponseEntity<Boolean> checkTrainingExists(@PathVariable Long trainingId) {
        log.debug("GET /internal/trainings/{}/exists", trainingId);
        return ResponseEntity.ok(trainingSessionService.checkTrainingExists(trainingId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingSessionDto> getTrainingById(@PathVariable("id") Long trainingId) {
        log.debug("GET /internal/trainings/{}", trainingId);
        return ResponseEntity.ok(trainingSessionService.getTrainingById(trainingId));
    }
}
