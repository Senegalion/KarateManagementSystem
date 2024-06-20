package com.example.karatemanagementsystem.repository;

import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findById(long id);

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Set<User> findByTrainingSessions(TrainingSession trainingSession);
}
