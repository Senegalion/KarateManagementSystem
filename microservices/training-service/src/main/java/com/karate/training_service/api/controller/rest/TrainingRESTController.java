package com.karate.training_service.api.controller.rest;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.service.TrainingSessionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/trainings")
public class TrainingRESTController {
    private final TrainingSessionService trainingSessionService;

    @GetMapping
    public ResponseEntity<List<TrainingSessionDto>> getAllTrainingSessionsForUserClub() {
        log.info("GET /trainings (current user's club)");
        return ResponseEntity.ok(trainingSessionService.getAllTrainingSessionsForCurrentUserClub());
    }

    @PostMapping("/create")
    public ResponseEntity<TrainingSessionDto> createTrainingSession(@RequestBody @Valid TrainingSessionRequestDto dto) {
        log.info("POST /trainings/create");
        TrainingSessionDto created = trainingSessionService.createTrainingSession(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingSession(@PathVariable("id") Long id) {
        log.info("DELETE /trainings/{}", id);
        trainingSessionService.deleteTrainingSession(id);
        return ResponseEntity.noContent().build();
    }
}
