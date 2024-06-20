package com.example.karatemanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference(value = "userReference")
    private User user;

    @ManyToOne
    @JsonBackReference
    private TrainingSession trainingSession;

    private String comment;

    private int starRating;
}