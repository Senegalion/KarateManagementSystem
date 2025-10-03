package com.karate.authservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.authservice.api.controller.rest.AuthRESTController;
import com.karate.authservice.api.dto.*;
import com.karate.authservice.api.exception.GlobalExceptionHandler;
import com.karate.authservice.domain.exception.UsernameWhileTryingToLogInNotFoundException;
import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.infrastructure.jwt.JwtAuthenticatorService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthRESTController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthRESTControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtAuthenticatorService jwtAuthenticatorService;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @Test
    void registerUser_created201_andPassesEncodedPasswordToService() throws Exception {
        // given
        var req = RegisterUserDto.builder()
                .username("john")
                .email("j@ex.com")
                .address(new AddressRequestDto("City", "Street", "1", "00-000"))
                .karateClubName("TOKYO")
                .karateRank("KYU_9")
                .role("USER")
                .password("plain")
                .build();

        when(passwordEncoder.encode("plain")).thenReturn("ENC");
        when(authService.register(any())).thenReturn(
                RegistrationResultDto.builder().userId(777L).username("john").email("j@ex.com").build()
        );

        // when // then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(777))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("j@ex.com"));

        // then (verify encoder + the dto passed to service has encoded password)
        verify(passwordEncoder).encode("plain");
        ArgumentCaptor<RegisterUserDto> captor = ArgumentCaptor.forClass(RegisterUserDto.class);
        verify(authService).register(captor.capture());
        RegisterUserDto passed = captor.getValue();
        // password must be encoded
        org.assertj.core.api.Assertions.assertThat(passed.password()).isEqualTo("ENC");
        // sanity on a couple of fields
        org.assertj.core.api.Assertions.assertThat(passed.username()).isEqualTo("john");
        org.assertj.core.api.Assertions.assertThat(passed.karateClubName()).isEqualTo("TOKYO");
    }

    @Test
    void registerUser_400_validationErrors_whenMissingFields() throws Exception {
        // given
        String body = "{}";

        // when // then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("username")));
    }

    @Test
    void registerUser_400_whenMalformedJson() throws Exception {
        // given
        String body = "not-a-json";

        // when // then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("missing or malformed")));
    }

    @Test
    void login_ok_200_returnsToken() throws Exception {
        // given
        var tokenReq = TokenRequestDto.builder()
                .username("john")
                .password("pw")
                .karateClubName("TOKYO")
                .build();

        doNothing().when(authService).validateUserForLogin(any(TokenRequestDto.class));
        when(jwtAuthenticatorService.authenticateAndGenerateToken(any(TokenRequestDto.class)))
                .thenReturn(LoginResponseDto.builder().username("john").token("JWT").build());

        // when // then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.token").value("JWT"));

        // then
        verify(authService).validateUserForLogin(any(TokenRequestDto.class));
        verify(jwtAuthenticatorService).authenticateAndGenerateToken(any(TokenRequestDto.class));
    }

    @Test
    void login_401_whenValidationFailsInService() throws Exception {
        // given
        var tokenReq = TokenRequestDto.builder()
                .username("bad")
                .password("pw")
                .karateClubName("NOPE")
                .build();

        doThrow(new UsernameWhileTryingToLogInNotFoundException("Invalid username or password"))
                .when(authService).validateUserForLogin(any(TokenRequestDto.class));

        // when // then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message", containsString("Invalid username or password")));
    }

    @Test
    void login_400_validationErrors_whenBodyMissingFields() throws Exception {
        // given
        String body = "{}";

        // when // then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Validation failed")));
    }
}
