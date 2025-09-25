package com.karate.clubservice.unit.repository;

import com.karate.clubservice.domain.model.KarateClubEntity;
import com.karate.clubservice.domain.model.KarateClubName;
import com.karate.clubservice.domain.repository.KarateClubRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create"
})
class KarateClubRepositoryDataJpaTest {

    @Autowired
    KarateClubRepository repository;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void saveAndFindById_returnsEntity() {
        // given
        var entity = KarateClubEntity.builder()
                .name(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE)
                .build();
        var saved = repository.save(entity);

        // when
        var found = repository.findById(saved.getKarateClubId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE);
    }

    @Test
    void findByName_returnsEntity_whenExists() {
        // given
        var e = repository.save(KarateClubEntity.builder()
                .name(KarateClubName.WOLOMINSKI_KLUB_SHORIN_RYU_KARATE)
                .build());

        // when
        Optional<KarateClubEntity> found =
                repository.findByName(KarateClubName.WOLOMINSKI_KLUB_SHORIN_RYU_KARATE);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getKarateClubId()).isEqualTo(e.getKarateClubId());
    }

    @Test
    void findByName_returnsEmpty_whenNotExists() {
        // given
        // (no data)

        // when
        Optional<KarateClubEntity> found =
                repository.findByName(KarateClubName.LUBELSKA_AKADEMIA_SPORTU);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void uniqueConstraint_onName_throwsDataIntegrityViolation() {
        // given
        repository.save(KarateClubEntity.builder()
                .name(KarateClubName.TARNOWSKA_AKADEMIA_KARATE_I_KOBUDO)
                .build());

        // when // then
        assertThatThrownBy(() ->
                repository.saveAndFlush(KarateClubEntity.builder()
                        .name(KarateClubName.TARNOWSKA_AKADEMIA_KARATE_I_KOBUDO)
                        .build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void enumStoredAsString_inNameColumn() {
        // given
        var name = KarateClubName.PIASECZYNSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE;
        var saved = repository.save(KarateClubEntity.builder().name(name).build());

        // when
        String raw = jdbc.queryForObject(
                "select name from karate_clubs where karate_club_id = ?",
                String.class,
                saved.getKarateClubId()
        );

        // then
        assertThat(raw).isEqualTo(name.name());
    }
}
