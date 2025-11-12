//package com.karate.feedback_service.it
//
//import com.karate.feedback_service.api.dto.FeedbackRequestDto
//import com.karate.feedback_service.api.dto.FeedbackResponseDto
//import com.karate.feedback_service.domain.repository.FeedbackRepository
//import com.karate.feedback_service.it.config.BaseIntegrationTest
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.http.MediaType
//
//@DisplayName("FeedbackRESTController – integration (secured endpoints)")
//class FeedbackRESTControllerIT : BaseIntegrationTest() {
//
//    @Autowired
//    lateinit var feedbackRepository: FeedbackRepository
//
//    @Test
//    @DisplayName("GET /feedbacks/{sid} returns 404 when not found (ROLE_USER)")
//    fun get_returns_404_when_not_found() {
//        // given
//
//        // when / then
//        webTestClient.get()
//            .uri("/feedbacks/{sid}", 999L)
//            .header("X-Test-User", "john")
//            .header("X-Test-Roles", "USER")
//            .exchange()
//            .expectStatus().isNotFound
//    }
//
//    @Test
//    @DisplayName("POST /feedbacks/{uid}/{sid} creates feedback (ROLE_ADMIN) and GET returns it")
//    fun post_creates_and_get_returns() {
//        // given
//        val req = FeedbackRequestDto("great training", 5)
//
//        // when / then (create)
//        webTestClient.post()
//            .uri("/feedbacks/{uid}/{sid}", 10L, 20L)
//            .header("X-Test-User", "admin")
//            .header("X-Test-Roles", "ADMIN")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(req)
//            .exchange()
//            .expectStatus().isCreated
//            .expectBody(FeedbackResponseDto::class.java)
//            .value {
//                assertThat(it.comment).isEqualTo("great training")
//                assertThat(it.starRating).isEqualTo(5)
//            }
//
//        // then (db)
//        val saved = feedbackRepository.findByUserIdAndTrainingSessionId(10L, 20L)
//        assertThat(saved).isPresent
//        assertThat(saved.get().comment).isEqualTo("great training")
//        assertThat(saved.get().starRating).isEqualTo(5)
//
//        // and GET
//        webTestClient.get()
//            .uri("/feedbacks/{sid}", 20L)
//            .header("X-Test-User", "john")
//            .header("X-Test-Roles", "USER")
//            .exchange()
//            .expectStatus().isOk
//            .expectBody(FeedbackResponseDto::class.java)
//            .value {
//                assertThat(it.comment).isEqualTo("great training")
//                assertThat(it.starRating).isEqualTo(5)
//            }
//    }
//
//    @Test
//    @DisplayName("POST /feedbacks/{uid}/{sid} requires ADMIN (403 for USER)")
//    fun post_requires_admin() {
//        // given
//        val req = FeedbackRequestDto("ok", 3)
//
//        // when / then
//        webTestClient.post()
//            .uri("/feedbacks/{uid}/{sid}", 1L, 2L)
//            .header("X-Test-User", "john")
//            .header("X-Test-Roles", "USER")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(req)
//            .exchange()
//            .expectStatus().isUnauthorized
//    }
//
//    @Test
//    @DisplayName("GET /feedbacks/{sid} requires auth (401 when missing)")
//    fun get_requires_auth() {
//        webTestClient.get()
//            .uri("/feedbacks/{sid}", 1L)
//            .exchange()
//            .expectStatus().isUnauthorized
//    }
//
//    @Test
//    @DisplayName("POST /feedbacks/{uid}/{sid} validation -> 400 (blank comment, rating null/out of range)")
//    fun post_validation_400() {
//        // blank comment
//        webTestClient.post()
//            .uri("/feedbacks/{uid}/{sid}", 1L, 2L)
//            .header("X-Test-User", "admin")
//            .header("X-Test-Roles", "ADMIN")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(mapOf("comment" to "   ", "starRating" to 3))
//            .exchange()
//            .expectStatus().isBadRequest
//
//        // rating null
//        webTestClient.post()
//            .uri("/feedbacks/{uid}/{sid}", 1L, 2L)
//            .header("X-Test-User", "admin")
//            .header("X-Test-Roles", "ADMIN")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(mapOf("comment" to "ok"))
//            .exchange()
//            .expectStatus().isBadRequest
//
//        // rating out of range
//        listOf(0, 6).forEach { invalid ->
//            webTestClient.post()
//                .uri("/feedbacks/{uid}/{sid}", 1L, 2L)
//                .header("X-Test-User", "admin")
//                .header("X-Test-Roles", "ADMIN")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(mapOf("comment" to "ok", "starRating" to invalid))
//                .exchange()
//                .expectStatus().isBadRequest
//        }
//    }
//}