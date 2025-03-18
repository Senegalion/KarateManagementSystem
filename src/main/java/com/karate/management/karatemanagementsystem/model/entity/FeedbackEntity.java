package com.karate.management.karatemanagementsystem.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "feedbacks")
public class FeedbackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSessionEntity trainingSessionEntity;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Column(name = "star_rating", nullable = false)
    private Integer starRating;
}
