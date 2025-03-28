package com.karate.management.karatemanagementsystem.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.management.karatemanagementsystem.model.staticdata.KarateClubName;
import com.karate.management.karatemanagementsystem.model.staticdata.RoleName;
import com.karate.management.karatemanagementsystem.model.dto.registration.RegisterUserDto;
import com.karate.management.karatemanagementsystem.model.dto.login.TokenRequestDto;
import com.karate.management.karatemanagementsystem.model.entity.KarateClubEntity;
import com.karate.management.karatemanagementsystem.model.entity.RoleEntity;
import com.karate.management.karatemanagementsystem.model.repository.KarateClubRepository;
import com.karate.management.karatemanagementsystem.model.repository.RoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthRESTControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KarateClubRepository karateClubRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.12"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeEach
    void setUp() {
        postgres.start();
        karateClubRepository.deleteAll();
        karateClubRepository.flush();

        KarateClubEntity clubEntity = new KarateClubEntity();
        clubEntity.setName(KarateClubName.LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO);
        karateClubRepository.saveAndFlush(clubEntity);

        roleRepository.deleteAll();
        roleRepository.flush();

        RoleEntity userRole = new RoleEntity();
        userRole.setName(RoleName.ROLE_USER);
        roleRepository.save(userRole);

        RoleEntity adminRole = new RoleEntity();
        adminRole.setName(RoleName.ROLE_ADMIN);
        roleRepository.save(adminRole);

        roleRepository.flush();
    }


    @AfterEach
    void tearDown() {
        postgres.stop();
    }

    @Test
    void should_return_201_created_when_user_registers_with_correct_data() throws Exception {
        // given
        RegisterUserDto registerUserDto = new RegisterUserDto(
                "testUser1",
                "someEmail@gmail.com",
                "someCity",
                "someStreet",
                "1",
                "12-345",
                "LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO",
                "KYU_10",
                "USER",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser1"))
                .andExpect(jsonPath("$.email").value("someEmail@gmail.com"));
    }

    @Test
    void should_return_400_bad_request_when_user_registered_with_invalid_data() throws Exception {
        // given
        RegisterUserDto invalidUserDto = new RegisterUserDto(null, "someEmail@gmail.com", "someCity",
                "someStreet", "1", "12-345", "INVALID_CLUB",
                "KYU_10", "USER", "password123");

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_200_ok_when_user_wants_to_login_with_correct_credentials() throws Exception {
        // given
        RegisterUserDto registerUserDto = new RegisterUserDto(
                "testUser2",
                "someEmail@gmail.com",
                "someCity",
                "someStreet",
                "1",
                "12-345",
                "LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO",
                "KYU_10",
                "USER",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDto)))
                .andExpect(status().isCreated());

        // given
        TokenRequestDto loginRequest = new TokenRequestDto("testUser2", "password123");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void should_return_401_unauthorized_when_user_tries_to_login_without_registration() throws Exception {
        // given
        TokenRequestDto invalidLoginRequest = new TokenRequestDto("wrongUser", "wrongPassword");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized());
    }
}