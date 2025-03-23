package com.karate.management.karatemanagementsystem.controller.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.management.karatemanagementsystem.model.data.KarateClubName;
import com.karate.management.karatemanagementsystem.model.data.KarateRank;
import com.karate.management.karatemanagementsystem.model.entity.AddressEntity;
import com.karate.management.karatemanagementsystem.model.entity.KarateClubEntity;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(content().string("No training sessions found"));
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
}