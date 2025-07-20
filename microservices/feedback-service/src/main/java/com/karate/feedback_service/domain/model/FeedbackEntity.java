package com.karate.feedback_service.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "feedbacks")
public class FeedbackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "training_session_id", nullable = false)
    private Long trainingSessionId;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Column(name = "star_rating", nullable = false)
    private Integer starRating;
}
