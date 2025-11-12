package com.karate.clubservice.it;

import com.karate.clubservice.domain.model.KarateClubEntity;
import com.karate.clubservice.domain.model.KarateClubName;
import com.karate.clubservice.domain.repository.KarateClubRepository;
import com.karate.clubservice.it.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KarateClubRESTController – integration (WebTestClient + Testcontainers)")
class KarateClubControllerIT extends BaseIntegrationTest {

    @Autowired
    KarateClubRepository repository;

    @Test
    @DisplayName("GET /clubs/by-name returns 200 and DTO when present")
    void getByName_returnsOk_andDto() {
        // given
        var saved = repository.saveAndFlush(KarateClubEntity.builder()
                .name(KarateClubName.WOLOMINSKI_KLUB_SHORIN_RYU_KARATE)
                .build());

        // when // then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/clubs/by-name")
                        .queryParam("name", KarateClubName.WOLOMINSKI_KLUB_SHORIN_RYU_KARATE.name())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Correlation-Id")
                .expectBody()
                .jsonPath("$.karateClubId").isEqualTo(saved.getKarateClubId().intValue())
                .jsonPath("$.name").isEqualTo(KarateClubName.WOLOMINSKI_KLUB_SHORIN_RYU_KARATE.name());
    }

    @Test
    @DisplayName("GET /clubs/by-name works with exact enum name (no case-insensitive contract)")
    void getByName_withExactEnumName() {
        // given
        repository.saveAndFlush(KarateClubEntity.builder()
                .name(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE)
                .build());

        // when // then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/clubs/by-name")
                        .queryParam("name", KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE.name())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE.name());
    }

    @Test
    @DisplayName("GET /clubs/by-name returns 404 when club is not in DB (but enum name is valid)")
    void getByName_returns404_whenNotFound() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/clubs/by-name")
                        .queryParam("name", KarateClubName.KLUB_OKINAWA_KARATE_DO_WARSZAWA.name())
                        .build())
                .exchange()
                .expectStatus().isNotFound(); // brak kontraktu na JSON body -> nie sprawdzamy treści
    }

    @Test
    @DisplayName("GET /clubs/by-name returns 400 on invalid enum name")
    void getByName_returns400_onInvalidName() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/clubs/by-name")
                        .queryParam("name", "NOT_EXISTING_ENUM")
                        .build())
                .exchange()
                .expectStatus().isBadRequest(); // domyślny błąd frameworka – bez JSON body
    }

    @Test
    @DisplayName("GET /clubs/by-id/{id} returns 200 and DTO when found")
    void getById_returnsOk_andDto() {
        // given
        var saved = repository.saveAndFlush(KarateClubEntity.builder()
                .name(KarateClubName.PIASECZYNSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE)
                .build());

        // when // then
        webTestClient.get()
                .uri("/clubs/by-id/{id}", saved.getKarateClubId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Correlation-Id")
                .expectBody()
                .jsonPath("$.karateClubId").isEqualTo(saved.getKarateClubId().intValue())
                .jsonPath("$.name").isEqualTo(KarateClubName.PIASECZYNSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE.name());
    }

    @Test
    @DisplayName("GET /clubs/by-id/{id} returns 404 when not found")
    void getById_returns404_whenNotFound() {
        webTestClient.get()
                .uri("/clubs/by-id/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound(); // brak kontraktu na JSON body
    }

    @Test
    @DisplayName("CorrelationIdFilter propagates X-Correlation-Id when provided")
    void correlationIdFilter_reusesIncomingHeader() {
        String cid = "test-correlation-id";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/clubs/by-name")
                        .queryParam("name", KarateClubName.LUBELSKA_AKADEMIA_SPORTU.name())
                        .build())
                .header("X-Correlation-Id", cid)
                .exchange()
                .expectHeader().valueEquals("X-Correlation-Id", cid);
    }
}
