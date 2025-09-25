package com.karate.clubservice.it;

import com.karate.clubservice.domain.model.KarateClubEntity;
import com.karate.clubservice.domain.model.KarateClubName;
import com.karate.clubservice.domain.model.dto.KarateClubDto;
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
        var saved = repository.save(KarateClubEntity.builder()
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
                .expectHeader().exists("X-Correlation-Id") // set by CorrelationIdFilter
                .expectBody(KarateClubDto.class)
                .value(dto -> {
                    // then
                    assertThat(dto.karateClubId()).isEqualTo(saved.getKarateClubId());
                    assertThat(dto.name()).isEqualTo(KarateClubName.WOLOMINSKI_KLUB_SHORIN_RYU_KARATE.name());
                });
    }

    @Test
    @DisplayName("GET /clubs/by-name is case-insensitive on enum name")
    void getByName_isCaseInsensitive() {
        // given
        repository.save(KarateClubEntity.builder()
                .name(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE)
                .build());
        var paramLower = "gdanski_klub_okinawa_shorin_ryu_karate";

        // when // then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/clubs/by-name").queryParam("name", paramLower).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(KarateClubDto.class)
                .value(dto -> {
                    // then
                    assertThat(dto.name()).isEqualTo(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE.name());
                });
    }

    @Test
    @DisplayName("GET /clubs/by-name returns 404 when club is not in DB (but enum name is valid)")
    void getByName_returns404_whenNotFound() {
        // given
        // (no data)

        // when // then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/clubs/by-name")
                        .queryParam("name", KarateClubName.KLUB_OKINAWA_KARATE_DO_WARSZAWA.name())
                        .build())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").value(msg -> assertThat((String) msg).contains("Club not found"));
    }

    @Test
    @DisplayName("GET /clubs/by-name returns 400 on invalid enum name")
    void getByName_returns400_onInvalidName() {
        // given
        String invalid = "NOT_EXISTING_ENUM";

        // when // then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/clubs/by-name").queryParam("name", invalid).build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").value(msg -> assertThat((String) msg).contains("Invalid club name"));
    }

    @Test
    @DisplayName("GET /clubs/by-id/{id} returns 200 and DTO when found")
    void getById_returnsOk_andDto() {
        // given
        var saved = repository.save(KarateClubEntity.builder()
                .name(KarateClubName.PIASECZYNSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE)
                .build());

        // when // then
        webTestClient.get()
                .uri("/clubs/by-id/{id}", saved.getKarateClubId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Correlation-Id")
                .expectBody(KarateClubDto.class)
                .value(dto -> {
                    // then
                    assertThat(dto.karateClubId()).isEqualTo(saved.getKarateClubId());
                    assertThat(dto.name()).isEqualTo(KarateClubName.PIASECZYNSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE.name());
                });
    }

    @Test
    @DisplayName("GET /clubs/by-id/{id} returns 404 when not found")
    void getById_returns404_whenNotFound() {
        // given
        long id = 999L;

        // when // then
        webTestClient.get()
                .uri("/clubs/by-id/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").value(msg -> assertThat((String) msg).contains("Club not found"));
    }

    @Test
    @DisplayName("CorrelationIdFilter propagates X-Correlation-Id when provided")
    void correlationIdFilter_reusesIncomingHeader() {
        // given
        String cid = "test-correlation-id";

        // when // then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/clubs/by-name")
                        .queryParam("name", KarateClubName.LUBELSKA_AKADEMIA_SPORTU.name())
                        .build())
                .header("X-Correlation-Id", cid)
                .exchange()
                .expectHeader().valueEquals("X-Correlation-Id", cid); // then
    }
}
