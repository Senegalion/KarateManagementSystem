package com.karate.feedback_service.unit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.karate.feedback_service.api.controller.rest.FeedbackRESTController
import com.karate.feedback_service.api.dto.FeedbackRequestDto
import com.karate.feedback_service.api.dto.FeedbackResponseDto
import com.karate.feedback_service.api.exception.GlobalExceptionHandler
import com.karate.feedback_service.domain.exception.FeedbackNotFoundException
import com.karate.feedback_service.domain.exception.TrainingSessionNotFoundException
import com.karate.feedback_service.domain.exception.UserNotSignedUpException
import com.karate.feedback_service.domain.service.FeedbackService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [FeedbackRESTController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler::class)
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "auth.jwt.secretKey=test-secret-at-least-32-chars",
        "auth.jwt.expirationDays=1",
        "auth.jwt.issuer=test"
    ]
)
@DisplayName("FeedbackRESTController – slice (MockMvc)")
class FeedbackRESTControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val om: ObjectMapper
) {

    @MockitoBean
    lateinit var feedbackService: FeedbackService

    // ---------- POST /feedbacks/{userId}/{trainingSessionId} ----------

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> returns 201 with body")
    fun post_returns_201_with_body() {
        // given
        val req = FeedbackRequestDto("great", 5)
        given(feedbackService.addFeedbackToUserForTrainingSession(1, 2, req))
            .willReturn(FeedbackResponseDto("great", 5))

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.comment").value("great"))
            .andExpect(jsonPath("$.starRating").value(5))

        // then
        verify(feedbackService).addFeedbackToUserForTrainingSession(1L, 2L, req)
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> returns 400 when body is missing")
    fun post_returns_400_when_body_missing() {
        // given

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Request body is missing or malformed"))
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> returns 400 when JSON is malformed")
    fun post_returns_400_when_json_malformed() {
        // given
        val malformed = "}"

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformed)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Request body is missing or malformed"))
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> returns 415 when unsupported media type")
    fun post_returns_415_when_unsupported_media_type() {
        // given
        val body = om.writeValueAsBytes(FeedbackRequestDto("ok", 3))

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.TEXT_PLAIN)
                .content(body)
        ).andExpect(status().isUnsupportedMediaType)
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> returns 400 on validation (blank comment)")
    fun post_returns_400_on_validation_blank_comment() {
        // given
        val req = mapOf("comment" to "   ", "starRating" to 3)

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("comment"))
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> returns 400 on validation (rating null)")
    fun post_returns_400_on_validation_rating_null() {
        // given
        val req = mapOf("comment" to "ok")

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("starRating"))
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> returns 400 on validation (rating out of range)")
    fun post_returns_400_on_validation_rating_out_of_range() {
        // given
        val invalids = listOf(0, 6)

        // when / then
        invalids.forEach { rating ->
            val req = mapOf("comment" to "ok", "starRating" to rating)

            mockMvc.perform(
                post("/feedbacks/1/2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(req))
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> maps UsernameNotFoundException to 401")
    fun post_maps_username_not_found_to_401() {
        // given
        val req = FeedbackRequestDto("ok", 3)
        given(feedbackService.addFeedbackToUserForTrainingSession(1, 2, req))
            .willThrow(UsernameNotFoundException("User not found"))

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("User not found"))
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> maps TrainingSessionNotFoundException to 404")
    fun post_maps_training_not_found_to_404() {
        // given
        val req = FeedbackRequestDto("ok", 3)
        given(feedbackService.addFeedbackToUserForTrainingSession(1, 2, req))
            .willThrow(TrainingSessionNotFoundException("Training session not found"))

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req))
        ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> maps UserNotSignedUpException to 403")
    fun post_maps_user_not_signed_up_to_403() {
        // given
        val req = FeedbackRequestDto("ok", 3)
        given(feedbackService.addFeedbackToUserForTrainingSession(1, 2, req))
            .willThrow(UserNotSignedUpException("User is not enrolled in the specified training session"))

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req))
        ).andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("POST /feedbacks/{uid}/{sid} -> maps generic Exception to 500")
    fun post_maps_generic_exception_to_500() {
        // given
        val req = FeedbackRequestDto("x", 1)
        given(feedbackService.addFeedbackToUserForTrainingSession(1, 2, req))
            .willThrow(RuntimeException("boom"))

        // when / then
        mockMvc.perform(
            post("/feedbacks/1/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req))
        ).andExpect(status().isInternalServerError)
    }

    // ---------- GET /feedbacks/{trainingSessionId} ----------

    @Test
    @DisplayName("GET /feedbacks/{sid} -> returns 200 with body")
    fun get_returns_200_with_body() {
        // given
        given(feedbackService.getFeedbackForSession(2))
            .willReturn(FeedbackResponseDto("ok", 4))

        // when / then
        mockMvc.perform(get("/feedbacks/2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.comment").value("ok"))
            .andExpect(jsonPath("$.starRating").value(4))

        // then (delegation)
        verify(feedbackService).getFeedbackForSession(eq(2L))
    }

    @Test
    @DisplayName("GET /feedbacks/{sid} -> maps UsernameNotFoundException to 401")
    fun get_maps_username_not_found_to_401() {
        // given
        given(feedbackService.getFeedbackForSession(2))
            .willThrow(UsernameNotFoundException("User not authenticated"))

        // when / then
        mockMvc.perform(get("/feedbacks/2"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("User not authenticated"))
    }

    @Test
    @DisplayName("GET /feedbacks/{sid} -> maps TrainingSessionNotFoundException to 404")
    fun get_maps_training_not_found_to_404() {
        // given
        given(feedbackService.getFeedbackForSession(2))
            .willThrow(TrainingSessionNotFoundException("Training session not found"))

        // when / then
        mockMvc.perform(get("/feedbacks/2"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET /feedbacks/{sid} -> maps UserNotSignedUpException to 403")
    fun get_maps_user_not_signed_up_to_403() {
        // given
        given(feedbackService.getFeedbackForSession(2))
            .willThrow(UserNotSignedUpException("User is not enrolled in the specified training session"))

        // when / then
        mockMvc.perform(get("/feedbacks/2"))
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("GET /feedbacks/{sid} -> maps FeedbackNotFoundException to 404")
    fun get_maps_feedback_not_found_to_404() {
        // given
        given(feedbackService.getFeedbackForSession(2))
            .willThrow(FeedbackNotFoundException("Feedback not found for this session"))

        // when / then
        mockMvc.perform(get("/feedbacks/2"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET /feedbacks/{sid} -> maps generic Exception to 500")
    fun get_maps_generic_exception_to_500() {
        // given
        given(feedbackService.getFeedbackForSession(2))
            .willThrow(RuntimeException("boom"))

        // when / then
        mockMvc.perform(get("/feedbacks/2"))
            .andExpect(status().isInternalServerError)
    }
}