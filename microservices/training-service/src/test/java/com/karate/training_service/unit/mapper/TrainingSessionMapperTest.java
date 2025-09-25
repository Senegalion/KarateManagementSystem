package com.karate.training_service.unit.mapper;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.infrastructure.persistence.mapper.TrainingSessionMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingSessionMapperTest {

    @Test
    void mapToTrainingSessionDto_mapsAllFields() {
        LocalDateTime s = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime e = s.plusHours(2);

        TrainingSessionEntity entity = new TrainingSessionEntity();
        entity.setTrainingSessionId(77L);
        entity.setStartTime(s);
        entity.setEndTime(e);
        entity.setDescription("desc");
        entity.setClubId(42L);

        TrainingSessionDto dto = TrainingSessionMapper.mapToTrainingSessionDto(entity);

        assertThat(dto.trainingSessionId()).isEqualTo(77L);
        assertThat(dto.startTime()).isEqualTo(s);
        assertThat(dto.endTime()).isEqualTo(e);
        assertThat(dto.description()).isEqualTo("desc");
    }
}
