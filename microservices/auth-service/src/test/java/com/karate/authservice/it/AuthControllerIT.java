package com.karate.authservice.it;

import com.karate.authservice.api.dto.*;
import com.karate.authservice.domain.model.AuthUserEntity;
import com.karate.authservice.domain.model.RoleEntity;
import com.karate.authservice.domain.model.RoleName;
import com.karate.authservice.domain.repository.AuthUserRepository;
import com.karate.authservice.domain.repository.RoleRepository;
import com.karate.authservice.domain.service.UpstreamGateway;
import com.karate.authservice.infrastructure.client.dto.KarateClubDto;
import com.karate.authservice.infrastructure.client.dto.UserInfoDto;
import com.karate.authservice.infrastructure.jwt.JwtAuthenticatorService;
import com.karate.authservice.infrastructure.messaging.UserEventProducer;
import com.karate.authservice.it.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Auth REST – integration")
class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    AuthUserRepository authUserRepository;

    @MockitoBean
    UpstreamGateway upstream;
    @MockitoBean
    JwtAuthenticatorService jwtAuthenticatorService;
    @MockitoBean
    UserEventProducer userEventProducer;

    private RoleEntity ensureRole(RoleName name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            var r = new RoleEntity();
            r.setName(name);
            return roleRepository.saveAndFlush(r);
        });
    }

    @Test
    @DisplayName("POST /auth/register -> 201 CREATED and persists, links userId from upstream")
    void register_created201_persistsAndLinks() {
        // given
        ensureRole(RoleName.ROLE_USER);
        when(upstream.getClubByName("TOKYO")).thenReturn(new KarateClubDto(21L, "TOKYO"));
        when(upstream.createUserAsync(any())).thenReturn(CompletableFuture.completedFuture(777L));
        when(upstream.getUserById(777L)).thenReturn(new UserInfoDto(777L, "j@ex.com", 21L, "KYU_9"));

        var req = RegisterUserDto.builder()
                .username("john")
                .email("j@ex.com")
                .address(new AddressRequestDto("C", "S", "1", "00-000"))
                .karateClubName("TOKYO")
                .karateRank("KYU_9")
                .role("USER")
                .password("pw")
                .build();

        // when && then
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RegistrationResultDto.class)
                .value(res -> {
                    assertThat(res.userId()).isEqualTo(777L);
                    assertThat(res.username()).isEqualTo("john");
                    assertThat(res.email()).isEqualTo("j@ex.com");
                });

        // and: DB state
        var saved = authUserRepository.findByUsername("john").orElseThrow();
        assertThat(saved.getUserId()).isEqualTo(777L);

        // and: event published
        verify(userEventProducer, atLeastOnce()).sendUserRegisteredEvent(any());
    }

    @Test
    @DisplayName("POST /auth/register -> 400 validation error (missing fields)")
    void register_400_validation() {
        // given && when && then
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed"));
    }

//    @Test
//    @DisplayName("POST /auth/register -> 400 malformed JSON")
//    void register_400_malformedJson() {
//        // given && when && then
//        webTestClient.post()
//                .uri("/auth/register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue("not-a-json")
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody()
//                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("missing or malformed"));
//    }

    @Test
    @DisplayName("POST /auth/register -> 409 conflict on duplicate username")
    void register_409_duplicateUsername() {
        // given
        var role = ensureRole(RoleName.ROLE_USER);
        var existing = AuthUserEntity.builder()
                .username("dup").password("ENC")
                .roleEntities(new HashSet<>(Set.of(role)))
                .build();
        authUserRepository.saveAndFlush(existing);

        when(upstream.getClubByName("TOKYO")).thenReturn(new KarateClubDto(21L, "TOKYO"));

        var req = RegisterUserDto.builder()
                .username("dup")
                .email("d@ex.com")
                .address(new AddressRequestDto("C", "S", "1", "00-000"))
                .karateClubName("TOKYO")
                .karateRank("KYU_9")
                .role("USER")
                .password("pw")
                .build();

        // when && then
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Username or email already exists");
    }

    @Test
    @DisplayName("POST /auth/login -> 200 OK returns token (club matches)")
    void login_200_ok() {
        // given
        var role = ensureRole(RoleName.ROLE_USER);
        var user = AuthUserEntity.builder()
                .userId(555L)
                .username("john")
                .password("$2a$10$encoded") // dowolny hash — walidacja hasła jest w JwtAuthenticatorService
                .roleEntities(new HashSet<>(Set.of(role)))
                .build();
        authUserRepository.saveAndFlush(user);

        when(upstream.getUserById(555L)).thenReturn(new UserInfoDto(555L, "j@ex.com", 21L, "KYU_9"));
        when(upstream.getClubById(21L)).thenReturn(new KarateClubDto(21L, "TOKYO"));
        when(jwtAuthenticatorService.authenticateAndGenerateToken(any()))
                .thenReturn(LoginResponseDto.builder().username("john").token("abc").build());

        var req = TokenRequestDto.builder()
                .username("john").password("pw").karateClubName("TOKYO")
                .build();

        // when && then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .value(dto -> {
                    assertThat(dto.username()).isEqualTo("john");
                    assertThat(dto.token()).isEqualTo("abc");
                });
    }

    @Test
    @DisplayName("POST /auth/login -> 401 when club mismatches")
    void login_401_clubMismatch() {
        // given
        var role = ensureRole(RoleName.ROLE_USER);
        var user = AuthUserEntity.builder()
                .userId(777L).username("mary").password("ENC")
                .roleEntities(new HashSet<>(Set.of(role))).build();
        authUserRepository.saveAndFlush(user);

        when(upstream.getUserById(777L)).thenReturn(new UserInfoDto(777L, "m@ex.com", 22L, "KYU_8"));
        when(upstream.getClubById(22L)).thenReturn(new KarateClubDto(22L, "OSAKA"));

        var req = TokenRequestDto.builder()
                .username("mary").password("pw").karateClubName("TOKYO")
                .build();

        // when && then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid username or password");
    }

    @Test
    @DisplayName("POST /auth/login -> 400 validation (missing fields)")
    void login_400_validation() {
        // given && when && then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed"));
    }
}
