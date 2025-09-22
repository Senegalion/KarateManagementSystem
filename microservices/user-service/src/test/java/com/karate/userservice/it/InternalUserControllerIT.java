package com.karate.userservice.it;

import com.karate.userservice.api.dto.NewUserRequestDto;
import com.karate.userservice.api.dto.UserInfoDto;
import com.karate.userservice.api.dto.UserPayload;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.dto.AddressDto;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.domain.service.UpstreamGateway;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import com.karate.userservice.it.config.BaseIntegrationTest;
import com.karate.userservice.it.config.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("InternalUserController – integration")
class InternalUserControllerIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UpstreamGateway upstream;

    @Autowired
    private WebTestClient client;

    private WebTestClient web;

    @BeforeEach
    void setUpClient() {
        this.web = client.mutate()
                .defaultHeader("X-Test-User", "internal")
                .defaultHeader("X-Test-Roles", "ADMIN")
                .build();
    }

    @Test
    @DisplayName("POST /internal/users creates user")
    void post_internal_users_creates_user() {
        // given
        var req = new NewUserRequestDto(
                100L, "john@ex.com", 5L, KarateRank.KYU_9.name(),
                new AddressDto("C", "S", "1", "00-000")
        );

        // when
        var res = web.post()
                .uri("/internal/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange();

        // then
        res.expectStatus().isOk()
                .expectBody(Long.class)
                .value(id -> assertThat(id).isEqualTo(100L));

        assertThat(userRepository.findById(100L)).isPresent();
    }

    @Test
    @DisplayName("GET /internal/users/{id} returns UserInfoDto")
    void get_internal_users_by_id_returns_user_info() {
        // given
        userRepository.save(TestData.user(200L, "a@b", 9L, KarateRank.KYU_10));

        // when
        var res = web.get()
                .uri("/internal/users/{id}", 200L)
                .exchange();

        // then
        res.expectStatus().isOk()
                .expectBody(UserInfoDto.class)
                .value(dto -> {
                    assertThat(dto.userId()).isEqualTo(200L);
                    assertThat(dto.email()).isEqualTo("a@b");
                    assertThat(dto.karateClubId()).isEqualTo(9L);
                    assertThat(dto.karateRank()).isEqualTo(KarateRank.KYU_10.name());
                });
    }

    @Test
    @DisplayName("GET /internal/users/{username}/club-id returns clubId")
    void get_internal_users_username_club_id_returns_id() {
        // given
        userRepository.save(TestData.user(300L, "m@ex", 42L, KarateRank.KYU_8));
        when(upstream.getAuthUserByUsername("mary"))
                .thenReturn(new AuthUserDto(300L, "mary", java.util.Set.of("ROLE_USER")));

        // when / then
        web.get()
                .uri("/internal/users/{username}/club-id", "mary")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(id -> assertThat(id).isEqualTo(42L));
    }

    @Test
    @DisplayName("GET /internal/users/{userId}/exists returns boolean")
    void get_internal_users_exists_returns_boolean() {
        // given
        userRepository.save(TestData.user(400L, "x@x", 1L, KarateRank.KYU_10));

        // when / then
        web.get()
                .uri("/internal/users/{id}/exists", 400L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(exists -> assertThat(exists).isTrue());

        web.get()
                .uri("/internal/users/{id}/exists", 401L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(exists -> assertThat(exists).isFalse());
    }

    @Test
    @DisplayName("GET /internal/users/payload/{id} returns payload with username from auth-service")
    void get_internal_users_payload_returns_payload() {
        // given
        userRepository.save(TestData.user(500L, "p@ex", 2L, KarateRank.KYU_9));
        when(upstream.getAuthUserByUserId(500L))
                .thenReturn(new AuthUserDto(500L, "payloadUser", java.util.Set.of("ROLE_USER")));

        // when / then
        web.get()
                .uri("/internal/users/payload/{id}", 500L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserPayload.class)
                .value(p -> {
                    assertThat(p.userId()).isEqualTo(500L);
                    assertThat(p.userEmail()).isEqualTo("p@ex");
                    assertThat(p.username()).isEqualTo("payloadUser");
                });
    }
}
