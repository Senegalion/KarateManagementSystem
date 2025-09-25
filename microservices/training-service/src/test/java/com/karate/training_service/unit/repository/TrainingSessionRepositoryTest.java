package com.karate.training_service.unit.repository;

import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.domain.repository.TrainingSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false",
        "spring.flyway.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import="
})
class TrainingSessionRepositoryTest {

    @Autowired
    TrainingSessionRepository repo;

    private TrainingSessionEntity entity(Long clubId, String desc) {
        TrainingSessionEntity e = new TrainingSessionEntity();
        e.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        e.setEndTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        e.setDescription(desc);
        e.setClubId(clubId);
        return e;
    }

    @Test
    void saveAndFindById_ok() {
        // given
        TrainingSessionEntity saved = repo.save(entity(1L, "x"));

        // when && then
        assertThat(saved.getTrainingSessionId()).isNotNull();

        var found = repo.findById(saved.getTrainingSessionId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("x");
    }

    @Test
    void findAllByClubId_returnsOnlyThatClub() {
        // given
        repo.save(entity(1L, "a"));
        repo.save(entity(2L, "b"));
        repo.save(entity(1L, "c"));

        // when
        List<TrainingSessionEntity> list = repo.findAllByClubId(1L);

        // then
        assertThat(list).extracting(TrainingSessionEntity::getDescription)
                .containsExactlyInAnyOrder("a", "c");
    }

    @Test
    void existsById_trueFalse() {
        // given
        var saved = repo.save(entity(3L, "z"));

        // when && then
        assertThat(repo.existsById(saved.getTrainingSessionId())).isTrue();
        assertThat(repo.existsById(999L)).isFalse();
    }

    @Test
    void constraints_descriptionNotNull_violates() {
        // given
        TrainingSessionEntity e = entity(1L, null);

        // when && then
        assertThatThrownBy(() -> {
            repo.saveAndFlush(e);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
