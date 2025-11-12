package com.karate.feedback_service.unit.service

import com.karate.feedback_service.api.dto.FeedbackRequestDto
import com.karate.feedback_service.domain.exception.FeedbackNotFoundException
import com.karate.feedback_service.domain.exception.TrainingSessionNotFoundException
import com.karate.feedback_service.domain.exception.UserNotSignedUpException
import com.karate.feedback_service.domain.model.FeedbackEntity
import com.karate.feedback_service.domain.repository.FeedbackRepository
import com.karate.feedback_service.domain.service.FeedbackService
import com.karate.feedback_service.infrastructure.client.AuthClient
import com.karate.feedback_service.infrastructure.client.EnrollmentClient
import com.karate.feedback_service.infrastructure.client.TrainingSessionClient
import com.karate.feedback_service.infrastructure.client.UserClient
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.*

@DisplayNameGeneration(ReplaceUnderscores::class)
@DisplayName("FeedbackService – unit")
class FeedbackServiceTest {

    private val repo: FeedbackRepository = mockk()
    private val userClient: UserClient = mockk()
    private val trainingClient: TrainingSessionClient = mockk()
    private val authClient: AuthClient = mockk()
    private val enrollmentClient: EnrollmentClient = mockk()

    private lateinit var service: FeedbackService

    @BeforeEach
    fun setUp() {
        service = FeedbackService(repo, userClient, trainingClient, authClient, enrollmentClient)
        SecurityContextHolder.clearContext()
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    // ---------------- addFeedbackToUserForTrainingSession ----------------

    @Test
    fun addFeedback_saves_and_returns_dto_happy_path() {
        // given
        every { userClient.checkUserExists(1) } returns true
        every { trainingClient.checkTrainingExists(2) } returns true
        every { enrollmentClient.checkUserEnrolledInSession(1, 2) } returns true
        every { repo.findByUserIdAndTrainingSessionId(1, 2) } returns Optional.empty()
        every { repo.save(any<FeedbackEntity>()) } answers {
            val e = it.invocation.args[0] as FeedbackEntity
            e.copy(feedbackId = 10L)
        }

        // when
        val out = service.addFeedbackToUserForTrainingSession(
            1, 2, FeedbackRequestDto(comment = "good", starRating = 5)
        )

        // then
        assertThat(out.comment).isEqualTo("good")
        assertThat(out.starRating).isEqualTo(5)
        verify(exactly = 1) { repo.save(any<FeedbackEntity>()) }
    }

    @Test
    fun addFeedback_throws_when_user_missing() {
        // given
        every { userClient.checkUserExists(1) } returns false

        // when / then
        assertThatThrownBy {
            service.addFeedbackToUserForTrainingSession(1, 2, FeedbackRequestDto("x", 3))
        }
            .isInstanceOf(UsernameNotFoundException::class.java)
            .hasMessage("User not found")

        verify(exactly = 0) { repo.save(any()) }
    }

    @Test
    fun addFeedback_throws_when_training_missing() {
        // given
        every { userClient.checkUserExists(1) } returns true
        every { trainingClient.checkTrainingExists(2) } returns false

        // when / then
        assertThatThrownBy {
            service.addFeedbackToUserForTrainingSession(1, 2, FeedbackRequestDto("x", 3))
        }
            .isInstanceOf(TrainingSessionNotFoundException::class.java)
            .hasMessage("Training session not found")

        verify(exactly = 0) { repo.save(any()) }
    }

    @Test
    fun addFeedback_throws_when_not_enrolled() {
        // given
        every { userClient.checkUserExists(1) } returns true
        every { trainingClient.checkTrainingExists(2) } returns true
        every { enrollmentClient.checkUserEnrolledInSession(1, 2) } returns false

        // when / then
        assertThatThrownBy {
            service.addFeedbackToUserForTrainingSession(1, 2, FeedbackRequestDto("x", 3))
        }
            .isInstanceOf(UserNotSignedUpException::class.java)
            .hasMessage("User is not enrolled in the specified training session")

        verify(exactly = 0) { repo.save(any()) }
    }

    // ---------------- getFeedbackForSession ----------------

    @Test
    fun getFeedback_returns_feedback_happy_path() {
        // given
        val auth = TestingAuthenticationToken("john", "N/A", "ROLE_USER").apply { isAuthenticated = true }
        SecurityContextHolder.getContext().authentication = auth

        every { authClient.getUserIdByUsername("john") } returns 1
        every { trainingClient.checkTrainingExists(2) } returns true
        every { enrollmentClient.checkUserEnrolledInSession(1, 2) } returns true
        every { repo.findByUserIdAndTrainingSessionId(1, 2) } returns Optional.of(
            FeedbackEntity(feedbackId = 10, userId = 1, trainingSessionId = 2, comment = "ok", starRating = 4)
        )

        // when
        val out = service.getFeedbackForSession(2)

        // then
        assertThat(out.comment).isEqualTo("ok")
        assertThat(out.starRating).isEqualTo(4)
    }

    @Test
    fun getFeedback_throws_when_no_auth_in_context() {
        // given
        SecurityContextHolder.clearContext()

        // when / then
        assertThatThrownBy { service.getFeedbackForSession(2) }
            .isInstanceOf(UsernameNotFoundException::class.java)
            .hasMessage("User not authenticated")
    }

    @Test
    fun getFeedback_throws_when_not_authenticated_flag_false() {
        // given
        val auth = TestingAuthenticationToken("john", "N/A", "ROLE_USER").apply { isAuthenticated = false }
        SecurityContextHolder.getContext().authentication = auth

        // when / then
        assertThatThrownBy { service.getFeedbackForSession(2) }
            .isInstanceOf(UsernameNotFoundException::class.java)
            .hasMessage("User not authenticated")
    }

    @Test
    fun getFeedback_throws_when_userId_not_resolved() {
        // given
        val auth = TestingAuthenticationToken("john", "N/A", "ROLE_USER").apply { isAuthenticated = true }
        SecurityContextHolder.getContext().authentication = auth

        every { authClient.getUserIdByUsername("john") } returns null

        // when / then
        assertThatThrownBy { service.getFeedbackForSession(2) }
            .isInstanceOf(UsernameNotFoundException::class.java)
            .hasMessage("User not found")
    }

    @Test
    fun getFeedback_throws_when_training_missing() {
        // given
        val auth = TestingAuthenticationToken("john", "N/A", "ROLE_USER").apply { isAuthenticated = true }
        SecurityContextHolder.getContext().authentication = auth

        every { authClient.getUserIdByUsername("john") } returns 1
        every { trainingClient.checkTrainingExists(2) } returns false

        // when / then
        assertThatThrownBy { service.getFeedbackForSession(2) }
            .isInstanceOf(TrainingSessionNotFoundException::class.java)
            .hasMessage("Training session not found")
    }

    @Test
    fun getFeedback_throws_when_not_enrolled() {
        // given
        val auth = TestingAuthenticationToken("john", "N/A", "ROLE_USER").apply { isAuthenticated = true }
        SecurityContextHolder.getContext().authentication = auth

        every { authClient.getUserIdByUsername("john") } returns 1
        every { trainingClient.checkTrainingExists(2) } returns true
        every { enrollmentClient.checkUserEnrolledInSession(1, 2) } returns false

        // when / then
        assertThatThrownBy { service.getFeedbackForSession(2) }
            .isInstanceOf(UserNotSignedUpException::class.java)
            .hasMessage("User is not enrolled in the specified training session")
    }

    @Test
    fun getFeedback_throws_when_feedback_missing() {
        // given
        val auth = TestingAuthenticationToken("john", "N/A", "ROLE_USER").apply { isAuthenticated = true }
        SecurityContextHolder.getContext().authentication = auth

        every { authClient.getUserIdByUsername("john") } returns 1
        every { trainingClient.checkTrainingExists(2) } returns true
        every { enrollmentClient.checkUserEnrolledInSession(1, 2) } returns true
        every { repo.findByUserIdAndTrainingSessionId(1, 2) } returns Optional.empty()

        // when / then
        assertThatThrownBy { service.getFeedbackForSession(2) }
            .isInstanceOf(FeedbackNotFoundException::class.java)
            .hasMessage("Feedback not found for this session")
    }
}
