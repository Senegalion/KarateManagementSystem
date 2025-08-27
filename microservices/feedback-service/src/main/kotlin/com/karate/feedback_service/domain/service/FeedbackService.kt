package com.karate.feedback_service.domain.service;

import com.karate.feedback_service.api.dto.FeedbackRequestDto
import com.karate.feedback_service.api.dto.FeedbackResponseDto
import com.karate.feedback_service.domain.exception.FeedbackNotFoundException
import com.karate.feedback_service.domain.exception.TrainingSessionNotFoundException
import com.karate.feedback_service.domain.exception.UserNotSignedUpException
import com.karate.feedback_service.domain.model.FeedbackEntity
import com.karate.feedback_service.domain.repository.FeedbackRepository
import com.karate.feedback_service.infrastructure.client.AuthClient
import com.karate.feedback_service.infrastructure.client.EnrollmentClient
import com.karate.feedback_service.infrastructure.client.TrainingSessionClient
import com.karate.feedback_service.infrastructure.client.UserClient
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val userClient: UserClient,
    private val trainingSessionClient: TrainingSessionClient,
    private val authClient: AuthClient,
    private val enrollmentClient: EnrollmentClient
) {

    @Transactional
    fun addFeedbackToUserForTrainingSession(
        userId: Long,
        trainingSessionId: Long,
        feedbackRequestDto: FeedbackRequestDto
    ): FeedbackResponseDto {
        if (userClient.checkUserExists(userId) != true) {
            throw UsernameNotFoundException("User not found")
        }

        if (trainingSessionClient.checkTrainingExists(trainingSessionId) != true) {
            throw TrainingSessionNotFoundException("Training session not found")
        }

        if (enrollmentClient.checkUserEnrolledInSession(userId, trainingSessionId) != true) {
            throw TrainingSessionNotFoundException("User is not enrolled in the specified training session")
        }

        val feedbackEntity = FeedbackEntity(
            userId = userId,
            trainingSessionId = trainingSessionId,
            comment = feedbackRequestDto.comment,
            starRating = feedbackRequestDto.starRating
        )

        val savedFeedback = feedbackRepository.save(feedbackEntity)
        return FeedbackResponseDto(savedFeedback.comment, savedFeedback.starRating)
    }

    @Transactional
    fun getFeedbackForSession(sessionId: Long): FeedbackResponseDto {
        println("SessionId: $sessionId")
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UsernameNotFoundException("User not authenticated")

        if (!authentication.isAuthenticated) {
            throw UsernameNotFoundException("User not authenticated")
        }

        val username = authentication.name
        val userId = authClient.getUserIdByUsername(username)
            ?: throw UsernameNotFoundException("User not found")
        println("UserId: $userId")

        if (trainingSessionClient.checkTrainingExists(sessionId) != true) {
            throw TrainingSessionNotFoundException("Training session not found")
        }

        if (enrollmentClient.checkUserEnrolledInSession(userId, sessionId) != true) {
            throw UserNotSignedUpException("User is not enrolled in the specified training session")
        }

        val feedback = feedbackRepository.findByUserIdAndTrainingSessionId(userId, sessionId)
            .orElseThrow { FeedbackNotFoundException("Feedback not found for this session") }

        return FeedbackResponseDto(feedback.comment, feedback.starRating)
    }
}
