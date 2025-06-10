package com.karate.management.karatemanagementsystem.domain.feedback.api;

import com.karate.management.karatemanagementsystem.domain.feedback.dto.FeedbackRequestDto;
import com.karate.management.karatemanagementsystem.domain.feedback.dto.FeedbackResponseDto;
import com.karate.management.karatemanagementsystem.domain.feedback.AdminFeedbackService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class FeedbackRESTController {
    private final AdminFeedbackService adminFeedbackService;

    @PostMapping("/feedback/{userId}/{trainingSessionId}")
    public ResponseEntity<FeedbackResponseDto> addFeedback(
            @PathVariable Long userId, @PathVariable Long trainingSessionId,
            @RequestBody @Valid FeedbackRequestDto feedbackRequestDto
    ) {
        FeedbackResponseDto feedback =
                adminFeedbackService.addFeedbackToUserForTrainingSession(userId, trainingSessionId, feedbackRequestDto);
        return new ResponseEntity<>(feedback, HttpStatus.CREATED);
    }
}
