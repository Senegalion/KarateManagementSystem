package com.karate.training_service.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "training_sessions")
public class TrainingSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_session_id")
    private Long trainingSessionId;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "club_id", nullable = false)
    private Long clubId;
}
