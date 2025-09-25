package com.karate.training_service.it;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.domain.repository.TrainingSessionRepository;
import com.karate.training_service.domain.service.UpstreamGateway;
import com.karate.training_service.it.config.BaseIntegrationTest;
import com.karate.training_service.it.config.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Training controllers – integration (secured endpoints)")
class TrainingControllerIT extends BaseIntegrationTest {

    @Autowired
    TrainingSessionRepository repo;

    @MockitoBean
    UpstreamGateway upstream;

    // ---------- /trainings (GET) ----------

    @Test
    @DisplayName("GET /trainings -> 401 when auth is missing")
    void getTrainings_requiresAuth() {
        webTestClient.get()
                .uri("/trainings")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /trainings (ROLE_USER) returns only sessions for user's club")
    void getTrainings_returnsOnlyUsersClub() {
        // given
        var now = LocalDateTime.of(2025, 1, 1, 10, 0);
        repo.saveAll(List.of(
                TestData.training(11L, "A", now, now.plusHours(1)),
                TestData.training(12L, "B", now, now.plusHours(2))
        ));
        when(upstream.getUserClubId("john")).thenReturn(11L);

        // when / then
        webTestClient.get()
                .uri("/trainings")
                .header("X-Test-User", "john")
                .header("X-Test-Roles", "USER")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TrainingSessionDto.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).description()).isEqualTo("A");
                });
    }

    // ---------- /trainings/create (POST) ----------

    @Test
    @DisplayName("POST /trainings/create -> 403 for USER (ADMIN required)")
    void postCreate_requiresAdmin() {
        var req = new TrainingSessionRequestDto(
                LocalDateTime.parse("2025-01-01T10:00:00"),
                LocalDateTime.parse("2025-01-01T11:00:00"),
                "ok");
        webTestClient.post()
                .uri("/trainings/create")
                .header("X-Test-User", "john")
                .header("X-Test-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /trainings/create (ADMIN) -> 201 and persists with clubId from upstream")
    void postCreate_created201_persists() {
        when(upstream.getUserClubId("admin")).thenReturn(77L);
        var req = new TrainingSessionRequestDto(
                LocalDateTime.parse("2025-01-01T10:00:00"),
                LocalDateTime.parse("2025-01-01T11:00:00"),
                "great"
        );

        webTestClient.post()
                .uri("/trainings/create")
                .header("X-Test-User", "admin")
                .header("X-Test-Roles", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TrainingSessionDto.class)
                .value(dto -> {
                    assertThat(dto.description()).isEqualTo("great");
                    assertThat(dto.startTime()).isEqualTo(req.startTime());
                });

        var all = repo.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getClubId()).isEqualTo(77L);
    }

    @Test
    @DisplayName("POST /trainings/create -> 400 JSR-380 validation (missing fields)")
    void postCreate_400_validationMissingFields() {
        webTestClient.post()
                .uri("/trainings/create")
                .header("X-Test-User", "admin")
                .header("X-Test-Roles", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed"));
    }

    @Test
    @DisplayName("POST /trainings/create -> 400 when body is malformed JSON")
    void postCreate_400_malformedJson() {
        webTestClient.post()
                .uri("/trainings/create")
                .header("X-Test-User", "admin")
                .header("X-Test-Roles", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("not-a-json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("missing or malformed"));
    }

    @Test
    @DisplayName("POST /trainings/create -> 400 when end<=start (domain exception)")
    void postCreate_400_invalidRange() {
        when(upstream.getUserClubId("admin")).thenReturn(1L);
        var req = new TrainingSessionRequestDto(
                LocalDateTime.parse("2025-01-01T10:00:00"),
                LocalDateTime.parse("2025-01-01T10:00:00"),
                "x"
        );

        webTestClient.post()
                .uri("/trainings/create")
                .header("X-Test-User", "admin")
                .header("X-Test-Roles", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("End time must be after start time"));
    }

    // ---------- /trainings/{id} (DELETE) ----------

    @Test
    @DisplayName("DELETE /trainings/{id} -> 403 for USER")
    void delete_requiresAdmin() {
        webTestClient.delete()
                .uri("/trainings/{id}", 1L)
                .header("X-Test-User", "john")
                .header("X-Test-Roles", "USER")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("DELETE /trainings/{id} (ADMIN) -> 204 when club matches")
    void delete_204_ok_whenClubMatches() {
        var now = LocalDateTime.of(2025, 1, 1, 10, 0);
        TrainingSessionEntity saved = repo.save(TestData.training(5L, "to-del", now, now.plusHours(1)));
        when(upstream.getUserClubId("admin")).thenReturn(5L);

        webTestClient.delete()
                .uri("/trainings/{id}", saved.getTrainingSessionId())
                .header("X-Test-User", "admin")
                .header("X-Test-Roles", "ADMIN")
                .exchange()
                .expectStatus().isNoContent();

        assertThat(repo.findById(saved.getTrainingSessionId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /trainings/{id} -> 404 when training does not exist")
    void delete_404_whenNotFound() {
        when(upstream.getUserClubId("admin")).thenReturn(1L);

        webTestClient.delete()
                .uri("/trainings/{id}", 999L)
                .header("X-Test-User", "admin")
                .header("X-Test-Roles", "ADMIN")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /trainings/{id} -> 403 when club mismatches")
    void delete_403_whenClubMismatch() {
        var now = LocalDateTime.of(2025, 1, 1, 10, 0);
        TrainingSessionEntity saved = repo.save(TestData.training(10L, "x", now, now.plusHours(1)));
        when(upstream.getUserClubId("admin")).thenReturn(20L);

        webTestClient.delete()
                .uri("/trainings/{id}", saved.getTrainingSessionId())
                .header("X-Test-User", "admin")
                .header("X-Test-Roles", "ADMIN")
                .exchange()
                .expectStatus().isForbidden();
    }

    // ---------- /internal/trainings ----------

    @Test
    @DisplayName("GET /internal/trainings/{id}/exists -> true/false (permitAll)")
    void internal_exists_trueFalse() {
        var now = LocalDateTime.of(2025, 1, 1, 10, 0);
        var saved = repo.save(TestData.training(1L, "a", now, now.plusHours(1)));

        webTestClient.get()
                .uri("/internal/trainings/{trainingId}/exists", saved.getTrainingSessionId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(b -> assertThat(b).isTrue());

        webTestClient.get()
                .uri("/internal/trainings/{trainingId}/exists", 999L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(b -> assertThat(b).isFalse());
    }

    @Test
    @DisplayName("GET /internal/trainings/{id} -> 200 and returns DTO (permitAll)")
    void internal_getById_ok() {
        var now = LocalDateTime.of(2025, 1, 1, 10, 0);
        var saved = repo.save(TestData.training(3L, "desc", now, now.plusHours(1)));

        webTestClient.get()
                .uri("/internal/trainings/{id}", saved.getTrainingSessionId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TrainingSessionDto.class)
                .value(dto -> {
                    assertThat(dto.trainingSessionId()).isEqualTo(saved.getTrainingSessionId());
                    assertThat(dto.description()).isEqualTo("desc");
                });
    }
}
