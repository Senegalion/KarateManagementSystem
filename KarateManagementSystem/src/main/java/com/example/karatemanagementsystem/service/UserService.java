package com.example.karatemanagementsystem.service;

import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(long id);
    Optional<User> getUserByEmail(String email);
    void deleteUser(User user);
    Set<User> findUsersByTrainingSession(TrainingSession session);
    void saveUser(User user);
}
