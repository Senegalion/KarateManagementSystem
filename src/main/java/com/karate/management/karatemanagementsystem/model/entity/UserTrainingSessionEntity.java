package com.karate.management.karatemanagementsystem.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users_training_sessions")
public class UserTrainingSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_training_sessions_id")
    private Long usersTrainingSessionsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSessionEntity trainingSessionEntity;
}
