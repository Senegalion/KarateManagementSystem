package com.karate.feedback_service.api.controller.rest;

import com.karate.feedback_service.api.dto.FeedbackRequestDto
import com.karate.feedback_service.api.dto.FeedbackResponseDto
import com.karate.feedback_service.api.dto.FeedbackResponseDtoExt
import com.karate.feedback_service.domain.service.FeedbackService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/feedbacks")
@Tag(name = "Feedbacks", description = "Feedbacks for trainings.")
@SecurityRequirement(name = "bearerAuth")
class FeedbackRESTController(
    private val feedbackService: FeedbackService
) {
    private val log = LoggerFactory.getLogger(FeedbackRESTController::class.java)

    @PostMapping("/{userId}/{trainingSessionId}")
    @Operation(summary = "Add feedback for user and training (admin)")
    fun addFeedback(
        @PathVariable userId: Long,
        @PathVariable trainingSessionId: Long,
        @Valid @RequestBody feedbackRequestDto: FeedbackRequestDto
    ): ResponseEntity<FeedbackResponseDto> {
        log.info("POST /feedbacks/{}/{} by user", userId, trainingSessionId)
        val feedback =
            feedbackService.addFeedbackToUserForTrainingSession(userId, trainingSessionId, feedbackRequestDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(feedback)
    }

    @GetMapping("/{trainingSessionId}")
    @Operation(summary = "Get feedback for training (current user)")
    fun getFeedback(
        @PathVariable trainingSessionId: Long
    ): ResponseEntity<FeedbackResponseDto> {
        log.info("GET /feedbacks/{}", trainingSessionId)
        val feedback = feedbackService.getFeedbackForSession(trainingSessionId)
        return ResponseEntity.ok(feedback)
    }

    @GetMapping("/admin/by-user/{userId}")
    @Operation(summary = "Get all feedbacks for given user (admin)")
    fun getAllForUser(@PathVariable userId: Long): ResponseEntity<List<FeedbackResponseDtoExt>> =
        ResponseEntity.ok(feedbackService.getAllForUser(userId))

    @GetMapping("/admin/by-training/{trainingSessionId}")
    @Operation(summary = "Get all feedbacks for training (admin)")
    fun getAllForTraining(@PathVariable trainingSessionId: Long): ResponseEntity<List<FeedbackResponseDtoExt>> =
        ResponseEntity.ok(feedbackService.getAllForTraining(trainingSessionId))

    @GetMapping("/admin/{userId}/{trainingSessionId}")
    @Operation(summary = "Get feedback for user and training (admin)")
    fun getForUserAndTraining(
        @PathVariable userId: Long,
        @PathVariable trainingSessionId: Long
    ): ResponseEntity<FeedbackResponseDto> =
        ResponseEntity.ok(feedbackService.getForUserAndTraining(userId, trainingSessionId))

    @GetMapping("/me")
    @Operation(summary = "Get my feedbacks")
    fun getMyFeedbacks(): ResponseEntity<List<FeedbackResponseDtoExt>> =
        ResponseEntity.ok(feedbackService.getAllForCurrentUser())
}
