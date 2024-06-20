package com.example.karatemanagementsystem.repository;

import com.example.karatemanagementsystem.model.Feedback;
import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Feedback findById(long id);

    List<Feedback> findByTrainingSession(TrainingSession trainingSession);

    List<Feedback> findByUser(User user);
}
