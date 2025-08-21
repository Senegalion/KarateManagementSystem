package com.karate.feedback_service.api.controller.rest;

import com.karate.feedback_service.api.dto.FeedbackRequestDto;
import com.karate.feedback_service.api.dto.FeedbackResponseDto;
import com.karate.feedback_service.domain.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/feedbacks")
public class FeedbackRESTController {
    private final FeedbackService feedbackService;

    @PostMapping("/{userId}/{trainingSessionId}")
    public ResponseEntity<FeedbackResponseDto> addFeedback(
            @PathVariable Long userId, @PathVariable Long trainingSessionId,
            @RequestBody @Valid FeedbackRequestDto feedbackRequestDto
    ) {
        FeedbackResponseDto feedback =
                feedbackService.addFeedbackToUserForTrainingSession(userId, trainingSessionId, feedbackRequestDto);
        return new ResponseEntity<>(feedback, HttpStatus.CREATED);
    }
}
