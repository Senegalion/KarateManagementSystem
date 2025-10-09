package com.karate.feedback_service.domain.service;

import com.karate.feedback_service.api.dto.FeedbackRequestDto
import com.karate.feedback_service.api.dto.FeedbackResponseDto
import com.karate.feedback_service.api.dto.FeedbackResponseDtoExt
import com.karate.feedback_service.domain.exception.FeedbackNotFoundException
import com.karate.feedback_service.domain.exception.TrainingSessionNotFoundException
import com.karate.feedback_service.domain.exception.UserNotSignedUpException
import com.karate.feedback_service.domain.model.FeedbackEntity
import com.karate.feedback_service.domain.repository.FeedbackRepository
import com.karate.feedback_service.infrastructure.client.AuthClient
import com.karate.feedback_service.infrastructure.client.EnrollmentClient
import com.karate.feedback_service.infrastructure.client.TrainingSessionClient
import com.karate.feedback_service.infrastructure.client.UserClient
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(FeedbackService::class.java)

    private fun toDto(e: FeedbackEntity) =
        FeedbackResponseDto(comment = e.comment, starRating = e.starRating)

    private fun toExt(e: FeedbackEntity) =
        FeedbackResponseDtoExt(
            feedbackId = e.feedbackId ?: 0L,
            userId = e.userId,
            trainingSessionId = e.trainingSessionId,
            comment = e.comment,
            starRating = e.starRating
        )

    @Transactional
    fun addFeedbackToUserForTrainingSession(
        userId: Long,
        trainingSessionId: Long,
        feedbackRequestDto: FeedbackRequestDto
    ): FeedbackResponseDto {
        log.info("Creating/updating feedback for userId={} trainingId={}", userId, trainingSessionId)

        if (!checkUser(userId)) {
            log.warn("User {} not found", userId)
            throw UsernameNotFoundException("User not found")
        }
        if (!checkTraining(trainingSessionId)) {
            log.warn("Training session {} not found", trainingSessionId)
            throw TrainingSessionNotFoundException("Training session not found")
        }
        if (!checkEnrollment(userId, trainingSessionId)) {
            log.warn("User {} is not enrolled in training {}", userId, trainingSessionId)
            throw UserNotSignedUpException("User is not enrolled in the specified training session")
        }

        val saved = feedbackRepository.findByUserIdAndTrainingSessionId(userId, trainingSessionId)
            .map { existing ->
                val updated = existing.copy(
                    comment = feedbackRequestDto.comment,
                    starRating = feedbackRequestDto.starRating
                )
                feedbackRepository.save(updated)
            }.orElseGet {
                feedbackRepository.save(
                    FeedbackEntity(
                        userId = userId,
                        trainingSessionId = trainingSessionId,
                        comment = feedbackRequestDto.comment,
                        starRating = feedbackRequestDto.starRating
                    )
                )
            }

        log.info("Feedback persisted id={} userId={} trainingId={}", saved.feedbackId, userId, trainingSessionId)
        return toDto(saved)
    }

    @Transactional(readOnly = true)
    fun getFeedbackForSession(sessionId: Long): FeedbackResponseDto {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw UsernameNotFoundException("User not authenticated")

        log.debug("Fetching feedback for training session={} by user={}", sessionId, auth.name)

        if (!auth.isAuthenticated) throw UsernameNotFoundException("User not authenticated")

        val username = auth.name
        val userId = getUserId(username) ?: throw UsernameNotFoundException("User not found")

        if (!checkTraining(sessionId)) {
            log.warn("Training session {} not found", sessionId)
            throw TrainingSessionNotFoundException("Training session not found")
        }
        if (!checkEnrollment(userId, sessionId)) {
            log.warn("User {} not enrolled in session {}", userId, sessionId)
            throw UserNotSignedUpException("User is not enrolled in the specified training session")
        }

        val fb = feedbackRepository.findByUserIdAndTrainingSessionId(userId, sessionId)
            .orElseThrow {
                log.error("No feedback found for user {} session {}", userId, sessionId)
                FeedbackNotFoundException("Feedback not found for this session")
            }

        log.info("Feedback retrieved userId={} sessionId={} stars={}", userId, sessionId, fb.starRating)
        return toDto(fb)
    }

    @Transactional(readOnly = true)
    fun getAllForUser(userId: Long): List<FeedbackResponseDtoExt> {
        log.info("Admin: listing feedbacks for userId={}", userId)
        return feedbackRepository.findAllByUserId(userId).map(::toExt)
    }

    @Transactional(readOnly = true)
    fun getAllForTraining(trainingSessionId: Long): List<FeedbackResponseDtoExt> {
        log.info("Admin: listing feedbacks for trainingSessionId={}", trainingSessionId)
        return feedbackRepository.findAllByTrainingSessionId(trainingSessionId).map(::toExt)
    }

    @Transactional(readOnly = true)
    fun getForUserAndTraining(userId: Long, trainingSessionId: Long): FeedbackResponseDto {
        log.info("Admin: get feedback for userId={} trainingSessionId={}", userId, trainingSessionId)
        val fb = feedbackRepository.findByUserIdAndTrainingSessionId(userId, trainingSessionId)
            .orElseThrow { FeedbackNotFoundException("Feedback not found") }
        return toDto(fb)
    }

    @Transactional(readOnly = true)
    fun getAllForCurrentUser(): List<FeedbackResponseDtoExt> {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw UsernameNotFoundException("User not authenticated")
        val userId = getUserId(auth.name) ?: throw UsernameNotFoundException("User not found")
        return feedbackRepository.findAllByUserId(userId).map {
            FeedbackResponseDtoExt(
                feedbackId = it.feedbackId ?: 0,
                userId = it.userId,
                trainingSessionId = it.trainingSessionId,
                comment = it.comment,
                starRating = it.starRating
            )
        }
    }

    @CircuitBreaker(name = "upstream", fallbackMethod = "userExistsFallback")
    fun checkUser(id: Long) = userClient.checkUserExists(id) == true

    @Suppress("unused")
    private fun userExistsFallback(id: Long, ex: Throwable) = false

    @CircuitBreaker(name = "upstream", fallbackMethod = "trainingExistsFallback")
    fun checkTraining(id: Long) = trainingSessionClient.checkTrainingExists(id) == true

    @Suppress("unused")
    private fun trainingExistsFallback(id: Long, ex: Throwable) = false

    @CircuitBreaker(name = "upstream", fallbackMethod = "enrolledFallback")
    fun checkEnrollment(uid: Long, sid: Long) = enrollmentClient.checkUserEnrolledInSession(uid, sid) == true

    @Suppress("unused")
    private fun enrolledFallback(uid: Long, sid: Long, ex: Throwable) = false

    @CircuitBreaker(name = "upstream", fallbackMethod = "userIdByUsernameFallback")
    fun getUserId(username: String) = authClient.getUserIdByUsername(username)

    @Suppress("unused")
    private fun userIdByUsernameFallback(username: String, ex: Throwable): Long? = null
}
