package com.karate.management.karatemanagementsystem.model.repository;

import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSessionRepository {
    List<TrainingSessionEntity> findAll();
}
