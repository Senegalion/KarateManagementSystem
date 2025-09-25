package com.karate.training_service.it.config;

import com.karate.training_service.domain.model.TrainingSessionEntity;

import java.time.LocalDateTime;

public final class TestData {
    private TestData() {
    }

    public static TrainingSessionEntity training(Long clubId, String desc,
                                                 LocalDateTime start, LocalDateTime end) {
        var e = new TrainingSessionEntity();
        e.setStartTime(start);
        e.setEndTime(end);
        e.setDescription(desc);
        e.setClubId(clubId);
        return e;
    }
}
