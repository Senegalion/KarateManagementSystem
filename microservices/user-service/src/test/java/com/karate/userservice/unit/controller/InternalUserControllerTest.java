package com.karate.userservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.userservice.api.controller.rest.InternalUserController;
import com.karate.userservice.api.dto.NewUserRequestDto;
import com.karate.userservice.api.dto.UserInfoDto;
import com.karate.userservice.api.dto.UserPayload;
import com.karate.userservice.api.exception.GlobalExceptionHandler;
import com.karate.userservice.domain.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InternalUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /internal/users returns 200 with created id")
    void post_internal_users_returns_200_with_id() throws Exception {
        // given
        var req = new NewUserRequestDto(
                100L, "u@ex.com", 5L, "KYU_10",
                new com.karate.userservice.domain.model.dto.AddressDto("C", "S", "1", "00-001")
        );
        when(userService.createUser(req)).thenReturn(100L);

        // when
        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))

                // then
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(userService).createUser(eq(req));
    }

    @Test
    @DisplayName("GET /internal/users/{id} returns user info")
    void get_internal_users_by_id_returns_info() throws Exception {
        // given
        var dto = new UserInfoDto(1L, "u@ex.com", 9L, "KYU_10", LocalDate.now());
        when(userService.getUserById(1L)).thenReturn(dto);

        // when
        mockMvc.perform(get("/internal/users/1"))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("u@ex.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("GET /internal/users/{username}/club-id returns club id")
    void get_internal_users_username_club_id_returns_value() throws Exception {
        // given
        when(userService.getCurrentUserClubIdByUsername("john")).thenReturn(42L);

        // when
        mockMvc.perform(get("/internal/users/john/club-id"))

                // then
                .andExpect(status().isOk())
                .andExpect(content().string("42"));

        verify(userService).getCurrentUserClubIdByUsername("john");
    }

    @Test
    @DisplayName("GET /internal/users/{userId}/exists returns boolean")
    void get_internal_users_exists_returns_boolean() throws Exception {
        // given
        when(userService.checkUserExists(5L)).thenReturn(true);

        // when
        mockMvc.perform(get("/internal/users/5/exists"))

                // then
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).checkUserExists(5L);
    }

    @Test
    @DisplayName("GET /internal/users/payload/{id} returns payload")
    void get_internal_users_payload_returns_payload() throws Exception {
        // given
        var payload = new UserPayload(7L, "u@ex.com", "john");
        when(userService.getUser(7L)).thenReturn(payload);

        // when
        mockMvc.perform(get("/internal/users/payload/7"))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.username").value("john"));

        verify(userService).getUser(7L);
    }
}
