package com.karate.management.karatemanagementsystem.controller.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingSessionUserRESTControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private UserRepository userRepository;

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
    void cleanUp() {
        userRepository.deleteAll();
        trainingSessionRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void should_return_200_ok_and_all_training_sessions_when_sessions_exist() throws Exception {
        // given
        TrainingSessionEntity session1 = new TrainingSessionEntity();
        session1.setDescription("Session 1");
        session1.setDate(LocalDateTime.of(2025, 3, 18, 20, 0, 0));

        TrainingSessionEntity session2 = new TrainingSessionEntity();
        session2.setDescription("Session 2");
        session2.setDate(LocalDateTime.of(2025, 3, 20, 20, 0, 0));

        trainingSessionRepository.saveAll(List.of(session1, session2));

        // when & then
        mockMvc.perform(get("/users/trainings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Session 1"))
                .andExpect(jsonPath("$[1].description").value("Session 2"))
                .andExpect(jsonPath("$[0].date").value("2025-03-18T20:00:00"))
                .andExpect(jsonPath("$[1].date").value("2025-03-20T20:00:00"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    @WithMockUser
    void should_return_404_not_found_when_no_sessions_exist() throws Exception {
        // given
        trainingSessionRepository.deleteAll();

        // when & then
        mockMvc.perform(get("/users/trainings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Training session not found"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void should_return_200_ok_and_current_user_details_when_user_is_authenticated() throws Exception {
        // given & when
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setPassword("password");
        userRepository.save(user);

        // when & then
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "unknownUser", roles = "USER")
    void should_throw_username_not_found_exception_when_user_does_not_exist() throws Exception {
        // given
        userRepository.deleteAll();

        // when & then
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Username does not exist"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void should_return_ok_200_when_user_registers_for_training_session() throws Exception {
        // given
        UserEntity testUser = new UserEntity();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        UserEntity savedUser = userRepository.save(testUser);

        TrainingSessionEntity testSession = new TrainingSessionEntity();
        testSession.setDate(LocalDateTime.now().plusDays(1));
        testSession.setDescription("Test Training Session");
        TrainingSessionEntity savedSession = trainingSessionRepository.save(testSession);

        // when
        ResultActions performForRegistrationForTrainingSession = mockMvc.perform(post("/users/trainings/signup/{sessionId}", testSession.getTrainingSessionId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        MvcResult resultForRegistrationForTrainingSession = performForRegistrationForTrainingSession
                .andExpect(status().isOk())
                .andReturn();
        String json = resultForRegistrationForTrainingSession.getResponse().getContentAsString();
        TrainingSessionRegistrationResponseDto trainingSessionRegistrationResponseDto =
                objectMapper.readValue(json, TrainingSessionRegistrationResponseDto.class);

        assertAll(
                () -> assertThat(trainingSessionRegistrationResponseDto).isNotNull(),
                () -> assertThat(Objects.requireNonNull(trainingSessionRegistrationResponseDto).message()).isEqualTo("Successfully signed up for the training session"),
                () -> assertThat(Objects.requireNonNull(trainingSessionRegistrationResponseDto).date()).isEqualTo(savedSession.getDate().truncatedTo(ChronoUnit.MINUTES)),
                () -> assertThat(Objects.requireNonNull(trainingSessionRegistrationResponseDto).description()).isEqualTo(savedSession.getDescription())
        );

        UserEntity updatedUser = userRepository.findByIdWithTrainingSessions(testUser.getUserId()).orElseThrow();
        int numberOfRegisteredTrainingSessions = updatedUser.getTrainingSessionEntities().size();
        assertThat(numberOfRegisteredTrainingSessions).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void should_return_404_not_found_when_user_tries_to_register_for_training_session_that_has_not_been_found() throws Exception {
        // given
        long nonExistingSessionId = 999L;

        UserEntity testUser = new UserEntity();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        userRepository.save(testUser);

        // when & then
        mockMvc.perform(post("/users/trainings/signup/{sessionId}", nonExistingSessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Training session not found"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void should_return_409_conflict_when_user_tries_to_register_for_training_session_that_has_earlier_registered() throws Exception {
        // given
        UserEntity testUser = new UserEntity();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        TrainingSessionEntity testSession = new TrainingSessionEntity();
        testSession.setDate(LocalDateTime.now().plusDays(1));
        testSession.setDescription("Test Training Session");
        trainingSessionRepository.save(testSession);

        testSession.getUserEntities().add(testUser);
        testUser.getTrainingSessionEntities().add(testSession);

        userRepository.save(testUser);
        trainingSessionRepository.save(testSession);

        // when & then
        mockMvc.perform(post("/users/trainings/signup/{sessionId}", testSession.getTrainingSessionId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("User is already signed up for this session"));
    }
}