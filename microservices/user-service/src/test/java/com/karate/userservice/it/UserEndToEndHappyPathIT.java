package com.karate.userservice.it;

import com.karate.userservice.api.dto.AddressRequestDto;
import com.karate.userservice.api.dto.NewUserRequestDto;
import com.karate.userservice.api.dto.UpdateUserRequestDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInfoDto;
import com.karate.userservice.api.dto.UserInformationDto;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.dto.AddressDto;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.domain.service.UpstreamGateway;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import com.karate.userservice.it.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Happy path – end-to-end user flow (integration)")
class UserEndToEndHappyPathIT extends BaseIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UpstreamGateway upstream;

    @Test
    @DisplayName("End-to-end: create -> get by id -> me -> put -> patch -> by-club -> delete")
    void end_to_end_user_flow() {
        // ---------------------------------------------------------
        // 0) Prepare convenient clients with default headers
        // ---------------------------------------------------------
        WebTestClient internal = client.mutate()
                .defaultHeader("X-Test-User", "internal")
                .defaultHeader("X-Test-Roles", "ADMIN")
                .build();

        WebTestClient asJohn = client.mutate()
                .defaultHeader("X-Test-User", "john")
                .defaultHeader("X-Test-Roles", "USER")
                .build();

        WebTestClient asAdmin = client.mutate()
                .defaultHeader("X-Test-User", "admin")
                .defaultHeader("X-Test-Roles", "ADMIN")
                .build();

        Long userId = 9000L;
        Long clubId = 123L;

        // ---------------------------------------------------------
        // 1) POST /internal/users — create user
        // ---------------------------------------------------------
        var createReq = new NewUserRequestDto(
                userId, "john@ex.com", clubId, KarateRank.KYU_9.name(),
                new AddressDto("CityA", "StreetA", "1", "00-001")
        );

        internal.post()
                .uri("/internal/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(id -> assertThat(id).isEqualTo(userId));

        assertThat(userRepository.findById(userId)).isPresent();

        // ---------------------------------------------------------
        // 2) GET /internal/users/{id} — returns UserInfoDto
        // ---------------------------------------------------------
        internal.get()
                .uri("/internal/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInfoDto.class)
                .value(dto -> {
                    assertThat(dto.userId()).isEqualTo(userId);
                    assertThat(dto.karateClubId()).isEqualTo(clubId);
                    assertThat(dto.karateRank()).isEqualTo(KarateRank.KYU_9.name());
                });

        // ---------------------------------------------------------
        // 3) GET /users/me — current user info (ROLE_USER)
        // ---------------------------------------------------------
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(userId, "john", Set.of("ROLE_USER")));
        when(upstream.getClubById(clubId))
                .thenReturn(new KarateClubDto(clubId, "TOKYO"));

        asJohn.get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInformationDto.class)
                .value(body -> {
                    assertThat(body.userId()).isEqualTo(userId);
                    assertThat(body.username()).isEqualTo("john");
                    assertThat(body.karateClubName()).isEqualTo("TOKYO");
                });

        // ---------------------------------------------------------
        // 4) PUT /users/me — full update (username + email + address)
        // ---------------------------------------------------------
        when(upstream.updateUsername(userId, "johnny"))
                .thenReturn(CompletableFuture.completedFuture(null));

        var putReq = new UpdateUserRequestDto(
                "johnny", "johnny@ex.com",
                new AddressRequestDto("CityB", "StreetB", "2", "11-111")
        );

        asJohn.put()
                .uri("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(putReq)
                .exchange()
                .expectStatus().isNoContent();

        verify(upstream).updateUsername(userId, "johnny");

        // Avoid LAZY exception: fetch with address eagerly
        var afterPut = userRepository.findWithAddressByUserId(userId).orElseThrow();
        assertThat(afterPut.getEmail()).isEqualTo("johnny@ex.com");
        assertThat(afterPut.getAddressEntity()).isNotNull();
        assertThat(afterPut.getAddressEntity().getCity()).isEqualTo("CityB");

        // ---------------------------------------------------------
        // 5) PATCH /users/me — partial update (email only)
        // ---------------------------------------------------------
        var patchReq = new UpdateUserRequestDto(null, "patched@ex.com", null);

        asJohn.patch()
                .uri("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(patchReq)
                .exchange()
                .expectStatus().isNoContent();

        var afterPatch = userRepository.findWithAddressByUserId(userId).orElseThrow();
        assertThat(afterPatch.getEmail()).isEqualTo("patched@ex.com");
        // address unchanged by patch
        assertThat(afterPatch.getAddressEntity().getCity()).isEqualTo("CityB");

        // ---------------------------------------------------------
        // 6) GET /users/by-club — admin lists users in club
        // ---------------------------------------------------------
        when(upstream.getClubByName("TOKYO"))
                .thenReturn(new KarateClubDto(clubId, "TOKYO"));
        when(upstream.getAuthUserByUserId(userId))
                .thenReturn(new AuthUserDto(userId, "johnny", Set.of("ROLE_USER")));

        asAdmin.get()
                .uri(uri -> uri.path("/users/by-club").queryParam("clubName", "TOKYO").build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserFromClubDto.class)
                .value(list -> assertThat(list).anySatisfy(u -> {
                    assertThat(u.userId()).isEqualTo(userId);
                    assertThat(u.username()).isEqualTo("johnny");
                    assertThat(u.email()).isEqualTo("patched@ex.com");
                    assertThat(u.karateRank()).isEqualTo(KarateRank.KYU_9.name());
                }));

        // ---------------------------------------------------------
        // 7) DELETE /users/me — user deletes himself
        // ---------------------------------------------------------
        when(upstream.deleteUser(userId))
                .thenReturn(CompletableFuture.completedFuture(null));

        asJohn.delete()
                .uri("/users/me")
                .exchange()
                .expectStatus().isNoContent();

        verify(upstream).deleteUser(userId);
        assertThat(userRepository.findById(userId)).isEmpty();

        // Optional follow-up: depends on your @ControllerAdvice mapping.
        // If you map "user not found" to 404, swap to .isNotFound().
        asJohn.get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isNotFound();
    }
}
