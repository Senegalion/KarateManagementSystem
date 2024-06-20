package com.example.karatemanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "training_sessions")
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;

    private String description;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "users_training_sessions",
            joinColumns = @JoinColumn(name = "training_sessions_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "trainingSession")
    private Set<Feedback> feedbacks = new HashSet<>();
}
