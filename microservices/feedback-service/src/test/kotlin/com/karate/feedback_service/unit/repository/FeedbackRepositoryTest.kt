package com.karate.feedback_service.unit.repository

import com.karate.feedback_service.domain.model.FeedbackEntity
import com.karate.feedback_service.domain.repository.FeedbackRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "auth.jwt.secretKey=test-secret-at-least-32-chars",
    "auth.jwt.expirationDays=1",
    "auth.jwt.issuer=test"
])
class FeedbackRepositoryTest @Autowired constructor(
    val repo: FeedbackRepository
) {

    @Test
    fun `findByUserIdAndTrainingSessionId zwraca encję gdy istnieje`() {
        val e = repo.save(
            FeedbackEntity(userId = 1, trainingSessionId = 2, comment = "ok", starRating = 4)
        )

        val out = repo.findByUserIdAndTrainingSessionId(1, 2)

        assertThat(out).isPresent
        assertThat(out.get().feedbackId).isEqualTo(e.feedbackId)
        assertThat(out.get().comment).isEqualTo("ok")
        assertThat(out.get().starRating).isEqualTo(4)
    }

    @Test
    fun `findByUserIdAndTrainingSessionId zwraca empty gdy brak`() {
        val out = repo.findByUserIdAndTrainingSessionId(9, 99)
        assertThat(out).isEmpty
    }
}