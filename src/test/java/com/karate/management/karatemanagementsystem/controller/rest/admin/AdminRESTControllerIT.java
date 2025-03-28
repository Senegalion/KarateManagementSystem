package com.karate.management.karatemanagementsystem.controller.rest.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.management.karatemanagementsystem.model.dto.feedback.FeedbackRequestDto;
import com.karate.management.karatemanagementsystem.model.dto.feedback.FeedbackResponseDto;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.FeedbackRepository;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminRESTControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.12"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    void setUp() {
        postgres.start();
    }

    @AfterAll
    void tearDown() {
        postgres.stop();
    }

    @BeforeEach
    void clear() {
        userRepository.deleteAll();
    }

    private TrainingSessionEntity createTrainingSession() {
        TrainingSessionEntity session = new TrainingSessionEntity();
        session.setDescription("Test Training Session");
        session.setDate(LocalDateTime.now());
        return trainingSessionRepository.save(session);
    }

    private UserEntity createUser(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail("someEmail@gmail.com");
        user.setPassword("password");
        return userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void should_return_201_created_when_admin_gives_feedback_to_the_user_for_training_session() throws Exception {
        // given
        UserEntity user = createUser("adminUser");
        TrainingSessionEntity session = createTrainingSession();

        session.getUserEntities().add(user);
        TrainingSessionEntity savedSession = trainingSessionRepository.save(session);
        user.getTrainingSessionEntities().add(savedSession);
        UserEntity enrolledUser = userRepository.save(user);
        Long enrolledUserUserId = enrolledUser.getUserId();

        Long trainingSessionId = savedSession.getTrainingSessionId();

        String comment = "Great session!";
        int starRating = 5;
        FeedbackRequestDto feedbackRequestDto = new FeedbackRequestDto(comment, starRating);

        // when
        ResultActions performForAddingFeedback = mockMvc.perform(post("/admin/feedback/{userId}/{trainingSessionId}", enrolledUserUserId, trainingSessionId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(feedbackRequestDto)));

        // then
        ResultActions resultActionsForAddingFeedback = performForAddingFeedback.andExpect(status().isCreated());
        String json = resultActionsForAddingFeedback.andReturn().getResponse().getContentAsString();
        FeedbackResponseDto feedbackResponseDto = objectMapper.readValue(json, FeedbackResponseDto.class);
        assertAll(
                () -> assertThat(feedbackResponseDto).isNotNull(),
                () -> assertThat(Objects.requireNonNull(feedbackResponseDto).comment()).isEqualTo(comment),
                () -> assertThat(Objects.requireNonNull(feedbackResponseDto).starRating()).isEqualTo(starRating)
        );
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void should_return_403_forbidden_when_normal_user_tries_to_give_feedback() throws Exception {
        // given
        UserEntity user = createUser("user");
        TrainingSessionEntity session = createTrainingSession();
        FeedbackRequestDto feedbackRequestDto = new FeedbackRequestDto("Good job!", 3);

        // when
        ResultActions performForAddingFeedback = mockMvc.perform(post("/admin/feedback/{userId}/{trainingSessionId}",
                user.getUserId(), session.getTrainingSessionId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(feedbackRequestDto)));

        // then
        performForAddingFeedback.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void should_return_404_not_found_when_feedback_is_given_to_non_existent_session() throws Exception {
        // given
        UserEntity user = createUser("adminUser");
        Long nonExistentSessionId = 999L;
        FeedbackRequestDto feedbackRequestDto = new FeedbackRequestDto("Awesome!", 5);

        // when
        ResultActions performForAddingFeedback = mockMvc.perform(post("/admin/feedback/{userId}/{trainingSessionId}",
                user.getUserId(), nonExistentSessionId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(feedbackRequestDto)));

        // then
        performForAddingFeedback.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void should_return_404_not_found_when_user_is_not_enrolled_in_session() throws Exception {
        // given
        UserEntity user = createUser("adminUser");
        TrainingSessionEntity session = createTrainingSession();
        FeedbackRequestDto feedbackRequestDto = new FeedbackRequestDto("Nice session!", 4);

        // when
        ResultActions performForAddingFeedback = mockMvc.perform(post("/admin/feedback/{userId}/{trainingSessionId}",
                user.getUserId(), session.getTrainingSessionId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(feedbackRequestDto)));

        // then
        performForAddingFeedback.andExpect(status().isNotFound());
    }
}