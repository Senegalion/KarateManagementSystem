package com.karate.feedback_service.api.controller.rest;

import com.karate.feedback_service.api.dto.FeedbackRequestDto
import com.karate.feedback_service.api.dto.FeedbackResponseDto
import com.karate.feedback_service.api.dto.FeedbackResponseDtoExt
import com.karate.feedback_service.domain.service.FeedbackService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/feedbacks")
class FeedbackRESTController(
    private val feedbackService: FeedbackService
) {
    private val log = LoggerFactory.getLogger(FeedbackRESTController::class.java)

    @PostMapping("/{userId}/{trainingSessionId}")
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
    fun getFeedback(
        @PathVariable trainingSessionId: Long
    ): ResponseEntity<FeedbackResponseDto> {
        log.info("GET /feedbacks/{}", trainingSessionId)
        val feedback = feedbackService.getFeedbackForSession(trainingSessionId)
        return ResponseEntity.ok(feedback)
    }

    @GetMapping("/admin/by-user/{userId}")
    fun getAllForUser(@PathVariable userId: Long): ResponseEntity<List<FeedbackResponseDtoExt>> =
        ResponseEntity.ok(feedbackService.getAllForUser(userId))

    @GetMapping("/admin/by-training/{trainingSessionId}")
    fun getAllForTraining(@PathVariable trainingSessionId: Long): ResponseEntity<List<FeedbackResponseDtoExt>> =
        ResponseEntity.ok(feedbackService.getAllForTraining(trainingSessionId))

    @GetMapping("/admin/{userId}/{trainingSessionId}")
    fun getForUserAndTraining(
        @PathVariable userId: Long,
        @PathVariable trainingSessionId: Long
    ): ResponseEntity<FeedbackResponseDto> =
        ResponseEntity.ok(feedbackService.getForUserAndTraining(userId, trainingSessionId))

    @GetMapping("/me")
    fun getMyFeedbacks(): ResponseEntity<List<FeedbackResponseDtoExt>> =
        ResponseEntity.ok(feedbackService.getAllForCurrentUser())
}
