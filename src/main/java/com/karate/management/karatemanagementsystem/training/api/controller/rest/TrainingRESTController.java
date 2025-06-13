package com.karate.management.karatemanagementsystem.training.api.controller.rest;

import com.karate.management.karatemanagementsystem.feedback.api.dto.FeedbackResponseDto;
import com.karate.management.karatemanagementsystem.training.api.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.training.api.dto.TrainingSessionRegistrationResponseDto;
import com.karate.management.karatemanagementsystem.domain.user.dto.UserDetailsDto;
import com.karate.management.karatemanagementsystem.training.domain.service.TrainingSessionService;
import com.karate.management.karatemanagementsystem.feedback.domain.service.UserFeedbackService;
import com.karate.management.karatemanagementsystem.domain.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class TrainingRESTController {
    private final TrainingSessionService trainingSessionService;
    private final UserService userService;
    private final UserFeedbackService userFeedbackService;

    @GetMapping("/me")
    public ResponseEntity<UserDetailsDto> getCurrentUserInfo() {
        UserDetailsDto userDetailsDto = userService.getCurrentUserInfo();
        return new ResponseEntity<>(userDetailsDto, HttpStatus.OK);
    }

    @GetMapping("/trainings")
    public ResponseEntity<List<TrainingSessionDto>> getAllTrainingSessions() {
        List<TrainingSessionDto> trainingSessions = trainingSessionService.getAllTrainingSessions();
        return ResponseEntity.ok(trainingSessions);
    }

    @GetMapping("/trainings/my")
    public ResponseEntity<List<TrainingSessionDto>> getUserTrainingSessions() {
        List<TrainingSessionDto> trainingSessions = trainingSessionService.getUserTrainingSessions();
        return ResponseEntity.ok(trainingSessions);
    }

    @PostMapping("/trainings/signup/{sessionId}")
    public ResponseEntity<TrainingSessionRegistrationResponseDto> signUpForTrainingSession(@PathVariable Long sessionId) {
        TrainingSessionRegistrationResponseDto trainingSessionRegistrationResponseDto =
                trainingSessionService.signUpForTrainingSession(sessionId);
        return ResponseEntity.ok(trainingSessionRegistrationResponseDto);
    }

    @DeleteMapping("/trainings/withdraw/{sessionId}")
    public ResponseEntity<TrainingSessionRegistrationResponseDto> signOutFromTrainingSession(@PathVariable Long sessionId) {
        TrainingSessionRegistrationResponseDto trainingSessionRegistrationResponseDto =
                trainingSessionService.withdrawFromTrainingSession(sessionId);
        return ResponseEntity.ok(trainingSessionRegistrationResponseDto);
    }

    @GetMapping("/trainings/{sessionId}/feedback")
    public ResponseEntity<FeedbackResponseDto> getFeedbackForTrainingSession(@PathVariable Long sessionId) {
        FeedbackResponseDto feedbackResponse = userFeedbackService.getFeedbackForSession(sessionId);
        return ResponseEntity.ok(feedbackResponse);
    }
}
