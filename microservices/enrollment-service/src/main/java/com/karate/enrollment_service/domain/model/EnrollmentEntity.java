package com.karate.enrollment_service.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;
}
