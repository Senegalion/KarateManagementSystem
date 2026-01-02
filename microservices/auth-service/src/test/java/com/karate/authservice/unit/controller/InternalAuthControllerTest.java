package com.karate.authservice.unit.controller;

import com.karate.authservice.api.controller.rest.InternalAuthController;
import com.karate.authservice.api.dto.AuthUserDto;
import com.karate.authservice.api.exception.GlobalExceptionHandler;
import com.karate.authservice.domain.service.AuthService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InternalAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InternalAuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AuthService authService;

    @Test
    void getByUserId_ok() throws Exception {
        when(authService.getAuthUserDto(10L))
                .thenReturn(new AuthUserDto(10L, "john", java.util.Set.of("ROLE_USER")));

        mvc.perform(get("/internal/users/{userId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void getByUsername_ok() throws Exception {
        when(authService.getAuthUserDtoByUsername("john"))
                .thenReturn(new AuthUserDto(10L, "john", java.util.Set.of("ROLE_ADMIN")));

        mvc.perform(get("/internal/users/by-username/{username}", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void getUsernamePayload_ok() throws Exception {
        when(authService.getUsername(10L)).thenReturn("john");

        mvc.perform(get("/internal/users/payload/{userId}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().string("john"));
    }

    @Test
    void getUserIdByUsername_ok() throws Exception {
        when(authService.getUserIdByUsername("john")).thenReturn(10L);

        mvc.perform(get("/internal/users/username/by-id/{username}", "john"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void updateUsername_ok() throws Exception {
        doNothing().when(authService).updateUsername(10L, "new");

        mvc.perform(put("/internal/users/{userId}/username", 10L)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("new"))
                .andExpect(status().isOk());

        verify(authService).updateUsername(10L, "new");
    }

    @Test
    void deleteUser_ok() throws Exception {
        doNothing().when(authService).deleteUser(10L);

        mvc.perform(delete("/internal/users/{userId}", 10L))
                .andExpect(status().isOk());

        verify(authService).deleteUser(10L);
    }
}
