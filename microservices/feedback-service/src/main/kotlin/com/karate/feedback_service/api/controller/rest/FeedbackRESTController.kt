package com.karate.feedback_service.api.controller.rest;

import com.karate.feedback_service.api.dto.FeedbackRequestDto
import com.karate.feedback_service.api.dto.FeedbackResponseDto
import com.karate.feedback_service.domain.service.FeedbackService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/feedbacks")
class FeedbackRESTController(
        private val feedbackService: FeedbackService
) {
    @PostMapping("/{userId}/{trainingSessionId}")
    fun addFeedback(
            @PathVariable userId: Long,
            @PathVariable trainingSessionId: Long,
            @Valid @RequestBody feedbackRequestDto: FeedbackRequestDto
    ): ResponseEntity<FeedbackResponseDto> {
        val feedback = feedbackService.addFeedbackToUserForTrainingSession(userId, trainingSessionId, feedbackRequestDto)
        return ResponseEntity(feedback, HttpStatus.CREATED)
    }
}
