package com.karate.management.karatemanagementsystem.domain.training;

import com.karate.management.karatemanagementsystem.domain.feedback.FeedbackEntity;
import com.karate.management.karatemanagementsystem.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "trainingSessionEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeedbackEntity> feedbackEntities;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "trainingSessionEntities")
    private Set<UserEntity> userEntities = new HashSet<>();
}
