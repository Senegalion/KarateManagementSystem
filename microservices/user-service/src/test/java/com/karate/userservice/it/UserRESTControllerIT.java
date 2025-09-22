package com.karate.userservice.it;

import com.karate.userservice.api.dto.AddressRequestDto;
import com.karate.userservice.api.dto.UpdateUserRequestDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInformationDto;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.domain.service.UpstreamGateway;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import com.karate.userservice.it.config.BaseIntegrationTest;
import com.karate.userservice.it.config.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("UserRESTController – integration (secured endpoints)")
class UserRESTControllerIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UpstreamGateway upstream;

    @Test
    @DisplayName("GET /users/me returns current user info (ROLE_USER)")
    void get_users_me_returns_current_user() {
        // given
        userRepository.save(TestData.user(700L, "me@ex", 21L, KarateRank.KYU_9));
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(700L, "john", Set.of("ROLE_USER")));
        when(upstream.getClubById(21L)).thenReturn(new KarateClubDto(21L, "TOKYO"));

        // when / then
        webTestClient.get()
                .uri("/users/me")
                .header("X-Test-User", "john")
                .header("X-Test-Roles", "USER")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInformationDto.class)
                .value(body -> {
                    assertThat(body.userId()).isEqualTo(700L);
                    assertThat(body.username()).isEqualTo("john");
                    assertThat(body.karateClubName()).isEqualTo("TOKYO");
                });
    }

    @Test
    @DisplayName("GET /users/by-club requires ADMIN and returns list")
    void get_users_by_club_requires_admin_and_returns_list() {
        // given
        userRepository.save(TestData.user(800L, "a@ex", 99L, KarateRank.KYU_10));
        userRepository.save(TestData.user(801L, "b@ex", 99L, KarateRank.KYU_9));
        when(upstream.getClubByName("OSAKA"))
                .thenReturn(new KarateClubDto(99L, "OSAKA"));
        when(upstream.getAuthUserByUserId(800L))
                .thenReturn(new AuthUserDto(800L, "u1", Set.of("ROLE_USER")));
        when(upstream.getAuthUserByUserId(801L))
                .thenReturn(new AuthUserDto(801L, "u2", Set.of("ROLE_USER")));

        // when / then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users/by-club").queryParam("clubName", "OSAKA").build())
                .header("X-Test-User", "admin")
                .header("X-Test-Roles", "ADMIN")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserFromClubDto.class)
                .value(list -> assertThat(list).hasSize(2));
    }

    @Test
    @DisplayName("PUT /users/me updates username & email & address (ROLE_USER)")
    void put_users_me_updates_user() {
        // given
        userRepository.save(TestData.user(810L, "old@ex", 33L, KarateRank.KYU_8));
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(810L, "john", Set.of("ROLE_USER")));
        // mock async updateUsername – zwróci CompletableFuture zakończone sukcesem
        when(upstream.updateUsername(810L, "newJohn"))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        var req = new UpdateUserRequestDto("newJohn", "new@ex",
                new AddressRequestDto("C", "S", "2", "11-111"));

        // when / then
        webTestClient.put()
                .uri("/users/me")
                .header("X-Test-User", "john")
                .header("X-Test-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNoContent();

        var refreshed = userRepository.findWithAddressByUserId(810L).orElseThrow();
        assertThat(refreshed.getEmail()).isEqualTo("new@ex");
        assertThat(refreshed.getAddressEntity().getCity()).isEqualTo("C");
    }

    @Test
    @DisplayName("PATCH /users/me updates partial fields (ROLE_USER)")
    void patch_users_me_updates_partial() {
        // given
        userRepository.save(TestData.user(820L, "xx@ex", 33L, KarateRank.KYU_8));
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(820L, "john", Set.of("ROLE_USER")));
        // username not changed; only email
        var req = new UpdateUserRequestDto(null, "yyy@ex", null);

        // when / then
        webTestClient.patch()
                .uri("/users/me")
                .header("X-Test-User", "john")
                .header("X-Test-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNoContent();

        var refreshed = userRepository.findById(820L).orElseThrow();
        assertThat(refreshed.getEmail()).isEqualTo("yyy@ex");
    }

    @Test
    @DisplayName("DELETE /users/me removes DB record and calls upstream.deleteUser (ROLE_USER)")
    void delete_users_me_deletes() {
        // given
        userRepository.save(TestData.user(830L, "del@ex", 77L, KarateRank.KYU_7));
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(830L, "john", Set.of("ROLE_USER")));
        when(upstream.deleteUser(830L))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        // when / then
        webTestClient.delete()
                .uri("/users/me")
                .header("X-Test-User", "john")
                .header("X-Test-Roles", "USER")
                .exchange()
                .expectStatus().isNoContent();

        assertThat(userRepository.findById(830L)).isEmpty();
    }
}
