package com.karate.management.karatemanagementsystem.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Table(name = "training_sessions")
@Getter
@Setter
public class TrainingSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_session_id")
    private Long trainingSessionId;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "trainingSessionEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeedbackEntity> feedbackEntities;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "trainingSessionEntity")
    private Set<UserTrainingSessionEntity> userTrainingSessionEntities;
}
