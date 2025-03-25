package com.karate.management.karatemanagementsystem.controller.rest.admin;

import com.karate.management.karatemanagementsystem.model.dto.feedback.FeedbackRequestDto;
import com.karate.management.karatemanagementsystem.model.dto.feedback.FeedbackResponseDto;
import com.karate.management.karatemanagementsystem.service.admin.FeedbackService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class FeedbackRESTController {
    private final FeedbackService feedbackService;

    @PostMapping("/feedback/{userId}/{trainingSessionId}")
    public ResponseEntity<FeedbackResponseDto> addFeedback(
            @PathVariable Long userId, @PathVariable Long trainingSessionId,
            @RequestBody @Valid FeedbackRequestDto feedbackRequestDto
    ) {
        FeedbackResponseDto feedback =
                feedbackService.addFeedbackToUserForTrainingSession(userId, trainingSessionId, feedbackRequestDto);
        return new ResponseEntity<>(feedback, HttpStatus.CREATED);
    }
}
