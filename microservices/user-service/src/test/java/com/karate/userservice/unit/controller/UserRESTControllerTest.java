package com.karate.userservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.userservice.api.controller.rest.UserRESTController;
import com.karate.userservice.api.dto.*;
import com.karate.userservice.api.exception.GlobalExceptionHandler;
import com.karate.userservice.domain.service.UserService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
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

    @Test
    @DisplayName("GET /users/me when service throws UserNotFoundException returns 404")
    void get_me_when_user_not_found_returns_404() throws Exception {
        when(userService.getCurrentUserInfo("john"))
                .thenThrow(new com.karate.userservice.domain.exception.UserNotFoundException("no user"));

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("no user"))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    @DisplayName("GET /users/me when service throws NoSuchElementException returns 404")
    void get_me_when_no_such_element_returns_404() throws Exception {
        when(userService.getCurrentUserInfo("john"))
                .thenThrow(new java.util.NoSuchElementException("missing"));

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("missing"))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    @DisplayName("GET /users/me when service throws UpstreamUnavailableException returns 503")
    void get_me_when_upstream_down_returns_503() throws Exception {
        when(userService.getCurrentUserInfo("john"))
                .thenThrow(new com.karate.userservice.domain.exception.UpstreamUnavailableException("auth down", new RuntimeException("x")));

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("auth down"))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    @DisplayName("GET /users/me when service throws RuntimeException returns 500")
    void get_me_when_generic_exception_returns_500() throws Exception {
        when(userService.getCurrentUserInfo("john"))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unexpected error: boom")))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    @DisplayName("PATCH /users/me with malformed JSON returns 400 and ErrorResponse message")
    void patch_me_with_malformed_json_returns_400() throws Exception {
        mockMvc.perform(patch("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this-is: not-json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    @DisplayName("GET /users/me when service throws FeignException.Conflict returns 409 with fixed message")
    void get_me_when_feign_conflict_returns_409() throws Exception {
        var req = Request.create(
                Request.HttpMethod.GET,
                "http://auth-service/users/me",
                java.util.Map.of(),
                null,
                new RequestTemplate()
        );

        var ex = new FeignException.Conflict(
                "conflict",
                req,
                null,
                java.util.Map.of()
        );

        when(userService.getCurrentUserInfo("john")).thenThrow(ex);

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username or email already exists"))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    @DisplayName("GET /users/me when service throws FeignClientException 404 returns 404 with upstream prefix")
    void get_me_when_feign_client_404_returns_404() throws Exception {
        var req = Request.create(
                Request.HttpMethod.GET,
                "http://club-service/whatever",
                java.util.Map.of(),
                null,
                new RequestTemplate()
        );

        var ex = new FeignException.FeignClientException(
                404,
                "not found",
                req,
                null,
                java.util.Map.of()
        );

        when(userService.getCurrentUserInfo("john")).thenThrow(ex);

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Upstream service error:")))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }

    @Test
    @DisplayName("GET /users/me when service throws FeignClientException with unknown status returns 400")
    void get_me_when_feign_client_unknown_status_returns_400() throws Exception {
        var req = Request.create(
                Request.HttpMethod.GET,
                "http://x",
                java.util.Map.of(),
                null,
                new RequestTemplate()
        );

        var ex = new FeignException.FeignClientException(
                499,
                "weird",
                req,
                null,
                java.util.Map.of()
        );

        when(userService.getCurrentUserInfo("john")).thenThrow(ex);

        mockMvc.perform(get("/users/me")
                        .principal(new UsernamePasswordAuthenticationToken("john", "pwd")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Upstream service error:")))
                .andExpect(jsonPath("$.path").value("/users/me"));
    }
}