package com.karate.training_service.api.controller.rest;

import com.karate.training_service.api.dto.TrainingRecurringRequestDto;
import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.service.TrainingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Trainings", description = "Trainings management for club members and admins.")
@SecurityRequirement(name = "bearerAuth")
public class TrainingRESTController {
    private final TrainingSessionService trainingSessionService;

    @GetMapping
    @Operation(
            summary = "Get trainings for current user's club",
            description = "Returns all training sessions that belong to the club of the currently authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Trainings fetched successfully")
    public ResponseEntity<List<TrainingSessionDto>> getAllTrainingSessionsForUserClub() {
        log.info("GET /trainings (current user's club)");
        return ResponseEntity.ok(trainingSessionService.getAllTrainingSessionsForCurrentUserClub());
    }

    @PostMapping("/create")
    @Operation(
            summary = "Create training session",
            description = "Creates a new training session in the current user's club. Only admins should be able to call this."
    )
    @ApiResponse(responseCode = "201", description = "Training created")
    public ResponseEntity<TrainingSessionDto> createTrainingSession(@RequestBody @Valid TrainingSessionRequestDto dto) {
        log.info("POST /trainings/create");
        TrainingSessionDto created = trainingSessionService.createTrainingSession(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete future training session",
            description = "Deletes a training session belonging to the current user's club. " +
                    "Past trainings cannot be deleted (to preserve feedback and history)."
    )
    @ApiResponse(responseCode = "204", description = "Training deleted")
    public ResponseEntity<Void> deleteTrainingSession(@PathVariable("id") Long id) {
        log.info("DELETE /trainings/{}", id);
        trainingSessionService.deleteTrainingSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create/recurring")
    @Operation(
            summary = "Create recurring training sessions",
            description = "Creates multiple trainings in a date range for selected weekdays. Only admins should be able to call this."
    )
    @ApiResponse(responseCode = "201", description = "Recurring trainings created")
    public ResponseEntity<List<TrainingSessionDto>> createRecurring(@RequestBody @Valid TrainingRecurringRequestDto dto) {
        log.info("POST /trainings/create/recurring from={} to={} days={} {}-{}",
                dto.fromDate(), dto.toDate(), dto.daysOfWeek(), dto.startTime(), dto.endTime());

        var created = trainingSessionService.createRecurringTrainings(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
