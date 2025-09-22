package com.karate.userservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.userservice.api.controller.rest.UserRESTController;
import com.karate.userservice.api.dto.AddressRequestDto;
import com.karate.userservice.api.dto.UpdateUserRequestDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInformationDto;
import com.karate.userservice.api.exception.GlobalExceptionHandler;
import com.karate.userservice.domain.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserRESTController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserRESTControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /users/by-club returns 200 with list")
    void get_users_by_club_returns_200_with_list() throws Exception {
        // given
        var list = List.of(
                new UserFromClubDto(1L, "john", "j@ex.com", Set.of("ROLE_USER"), "KYU_10"),
                new UserFromClubDto(2L, "mary", "m@ex.com", Set.of("ROLE_ADMIN"), "KYU_9")
        );
        when(userService.getUsersFromClubByName("TOKYO")).thenReturn(list);

        // when
        mockMvc.perform(get("/users/by-club").param("clubName", "TOKYO"))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService).getUsersFromClubByName("TOKYO");
    }

    @Test
    @DisplayName("GET /users/me returns 200 with current user info")
    void get_me_returns_200_with_current_user_info() throws Exception {
        // given
        var dto = new UserInformationDto(
                10L, "john", "j@ex.com", "TOKYO", "KYU_10", Set.of("ROLE_USER")
        );
        when(userService.getCurrentUserInfo("john")).thenReturn(dto);

        // when
        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.karateClubName").value("TOKYO"));

        verify(userService).getCurrentUserInfo("john");
    }

    @Test
    @DisplayName("PUT /users/me with valid body returns 204")
    void put_me_with_valid_body_returns_204() throws Exception {
        // given
        var body = new UpdateUserRequestDto(
                "newjohn",
                "new@ex.com",
                new AddressRequestDto("C", "S", "1", "00-001")
        );

        // when
        mockMvc.perform(put("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))

                // then
                .andExpect(status().isNoContent());

        verify(userService).updateCurrentUser(eq("john"), eq(body));
    }

    @Test
    @DisplayName("PUT /users/me with invalid body returns 400 (validation)")
    void put_me_with_invalid_body_returns_400() throws Exception {
        // given (invalid email and null address)
        var body = """
                {"username":"john","email":"not-email","address":null}
                """;

        // when
        mockMvc.perform(put("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/me partial body returns 204 and delegates to service")
    void patch_me_partial_returns_204() throws Exception {
        // given
        var body = new UpdateUserRequestDto(null, "changed@ex.com",
                new AddressRequestDto(null, "New Street", null, null));

        // when
        mockMvc.perform(patch("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))

                // then
                .andExpect(status().isNoContent());

        verify(userService).patchCurrentUser("john", body);
    }

    @Test
    @DisplayName("DELETE /users/me returns 204 and calls service")
    void delete_me_returns_204_and_calls_service() throws Exception {
        // given

        // when
        mockMvc.perform(delete("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))

                // then
                .andExpect(status().isNoContent());

        verify(userService).deleteCurrentUser("john");
    }
}