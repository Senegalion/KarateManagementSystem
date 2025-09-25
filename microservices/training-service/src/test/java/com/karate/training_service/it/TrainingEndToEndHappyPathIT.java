package com.karate.training_service.it;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.domain.repository.TrainingSessionRepository;
import com.karate.training_service.domain.service.UpstreamGateway;
import com.karate.training_service.it.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Happy path – end-to-end training flow (integration)")
class TrainingEndToEndHappyPathIT extends BaseIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private TrainingSessionRepository repo;

    @MockitoBean
    private UpstreamGateway upstream;

    @Test
    @DisplayName("End-to-end: admin creates (club=77) -> user lists (club=77 only) -> internal exists/get -> admin deletes -> user lists empty -> internal exists=false")
    void end_to_end_training_flow() {
        // ---------------------------------------------------------
        // 0) Clients with default headers
        // ---------------------------------------------------------
        WebTestClient internal = client.mutate()
                .defaultHeader("X-Test-User", "internal")
                .defaultHeader("X-Test-Roles", "ADMIN")
                .build();

        WebTestClient asAdmin = client.mutate()
                .defaultHeader("X-Test-User", "admin")
                .defaultHeader("X-Test-Roles", "ADMIN")
                .build();

        WebTestClient asBossOtherClub = client.mutate()
                .defaultHeader("X-Test-User", "boss")
                .defaultHeader("X-Test-Roles", "ADMIN")
                .build();

        WebTestClient asJohn = client.mutate()
                .defaultHeader("X-Test-User", "john")
                .defaultHeader("X-Test-Roles", "USER")
                .build();

        // ---------------------------------------------------------
        // 1) Upstream stubs: mapping user -> clubId
        // ---------------------------------------------------------
        long club77 = 77L;
        long club88 = 88L;
        when(upstream.getUserClubId("admin")).thenReturn(club77);
        when(upstream.getUserClubId("john")).thenReturn(club77);
        when(upstream.getUserClubId("boss")).thenReturn(club88);

        // ---------------------------------------------------------
        // 2) Admin creates a training in clubId=77
        // ---------------------------------------------------------
        var start = LocalDateTime.parse("2025-01-01T10:00:00");
        var end = LocalDateTime.parse("2025-01-01T11:00:00");

        var createReq1 = new TrainingSessionRequestDto(start, end, "kata");
        var createdId = new AtomicReference<Long>();

        asAdmin.post()
                .uri("/trainings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createReq1)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TrainingSessionDto.class)
                .value(dto -> {
                    assertThat(dto.description()).isEqualTo("kata");
                    assertThat(dto.startTime()).isEqualTo(start);
                    assertThat(dto.endTime()).isEqualTo(end);
                    createdId.set(dto.trainingSessionId());
                });

        // confirm persisted entity + clubId=77
        var persisted = repo.findById(createdId.get()).orElseThrow();
        assertThat(persisted.getClubId()).isEqualTo(club77);

        // ---------------------------------------------------------
        // 3) Second training in a different club (88) — should not appear for John
        // ---------------------------------------------------------
        var createReq2 = new TrainingSessionRequestDto(
                LocalDateTime.parse("2025-01-02T10:00:00"),
                LocalDateTime.parse("2025-01-02T11:00:00"),
                "kumite");

        asBossOtherClub.post()
                .uri("/trainings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createReq2)
                .exchange()
                .expectStatus().isCreated();

        // ---------------------------------------------------------
        // 4) Internal: exists + get by ID of the created training (permitAll)
        // ---------------------------------------------------------
        internal.get()
                .uri("/internal/trainings/{id}/exists", createdId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(b -> assertThat(b).isTrue());

        internal.get()
                .uri("/internal/trainings/{id}", createdId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TrainingSessionDto.class)
                .value(dto -> {
                    assertThat(dto.trainingSessionId()).isEqualTo(createdId.get());
                    assertThat(dto.description()).isEqualTo("kata");
                });

        // ---------------------------------------------------------
        // 5) User (ROLE_USER, club=77) sees only their own trainings
        // ---------------------------------------------------------
        asJohn.get()
                .uri("/trainings")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TrainingSessionDto.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).description()).isEqualTo("kata");
                });

        // ---------------------------------------------------------
        // 6) Admin deletes the training from club=77
        // ---------------------------------------------------------
        asAdmin.delete()
                .uri("/trainings/{id}", createdId.get())
                .exchange()
                .expectStatus().isNoContent();

        // repo: only the training from club 88 remains
        List<TrainingSessionEntity> all = repo.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getDescription()).isEqualTo("kumite");
        assertThat(all.get(0).getClubId()).isEqualTo(club88);

        // ---------------------------------------------------------
        // 7) After deletion: John no longer sees any trainings, exists=false
        // ---------------------------------------------------------
        asJohn.get()
                .uri("/trainings")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TrainingSessionDto.class)
                .value(list -> assertThat(list).isEmpty());

        internal.get()
                .uri("/internal/trainings/{id}/exists", createdId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(b -> assertThat(b).isFalse());
    }
}
