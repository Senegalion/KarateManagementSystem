package com.karate.management.karatemanagementsystem.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Table(name = "training_sessions")
public class TrainingSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_session_id")
    private Long trainingSessionId;

    @Column(name = "username", nullable = false)
    private LocalDateTime date;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "trainingSessionEntity")
    private Set<FeedbackEntity> feedbackEntities;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "trainingSessionEntity")
    private Set<UserTrainingSessionEntity> userTrainingSessionEntities;
}
