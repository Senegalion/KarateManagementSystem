package com.karate.feedback_service.it

import com.karate.feedback_service.api.dto.FeedbackRequestDto
import com.karate.feedback_service.api.dto.FeedbackResponseDto
import com.karate.feedback_service.domain.repository.FeedbackRepository
import com.karate.feedback_service.it.config.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@DisplayName("Happy path – end-to-end feedback flow (integration)")
class FeedbackEndToEndHappyPathIT : BaseIntegrationTest() {

    @Autowired
    lateinit var repo: FeedbackRepository

    @Test
    @DisplayName("End-to-end: POST -> GET -> validation -> GET 404")
    fun end_to_end_feedback_flow() {
        val asAdmin: WebTestClient = webTestClient.mutate()
            .defaultHeader("X-Test-User", "admin")
            .defaultHeader("X-Test-Roles", "ADMIN")
            .build()

        val asUser: WebTestClient = webTestClient.mutate()
            .defaultHeader("X-Test-User", "john")
            .defaultHeader("X-Test-Roles", "USER")
            .build()

        val userId = 1001L
        val sessionId = 2002L

        // 1) POST create feedback
        val req = FeedbackRequestDto("awesome", 5)

        asAdmin.post()
            .uri("/feedbacks/{uid}/{sid}", userId, sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
            .expectBody(FeedbackResponseDto::class.java)
            .value {
                assertThat(it.comment).isEqualTo("awesome")
                assertThat(it.starRating).isEqualTo(5)
            }

        val saved = repo.findByUserIdAndTrainingSessionId(userId, sessionId).orElseThrow()
        assertThat(saved.comment).isEqualTo("awesome")
        assertThat(saved.starRating).isEqualTo(5)

        // 2) GET returns the same
        asUser.get()
            .uri("/feedbacks/{sid}", sessionId)
            .exchange()
            .expectStatus().isOk
            .expectBody(FeedbackResponseDto::class.java)
            .value {
                assertThat(it.comment).isEqualTo("awesome")
                assertThat(it.starRating).isEqualTo(5)
            }

        // 3) POST validation error (bad rating)
        asAdmin.post()
            .uri("/feedbacks/{uid}/{sid}", userId, 3333L)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("comment" to "ok", "starRating" to 6))
            .exchange()
            .expectStatus().isBadRequest

        // 4) GET for non-existing session -> 404
        asUser.get()
            .uri("/feedbacks/{sid}", 99999L)
            .exchange()
            .expectStatus().isNotFound
    }
}