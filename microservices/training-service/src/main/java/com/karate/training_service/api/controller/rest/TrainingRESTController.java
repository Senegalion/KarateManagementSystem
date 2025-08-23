package com.karate.training_service.api.controller.rest;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.service.TrainingSessionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/trainings")
public class TrainingRESTController {
    private final TrainingSessionService trainingSessionService;

    @GetMapping
    public ResponseEntity<List<TrainingSessionDto>> getAllTrainingSessionsForUserClub() {
        List<TrainingSessionDto> trainingSessions = trainingSessionService.getAllTrainingSessionsForCurrentUserClub();
        return ResponseEntity.ok(trainingSessions);
    }

    @PostMapping("/create")
    public ResponseEntity<TrainingSessionDto> createTrainingSession(@RequestBody @Valid TrainingSessionRequestDto dto) {
        TrainingSessionDto created = trainingSessionService.createTrainingSession(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingSession(@PathVariable("id") Long id) {
        trainingSessionService.deleteTrainingSession(id);
        return ResponseEntity.noContent().build();
    }
}
