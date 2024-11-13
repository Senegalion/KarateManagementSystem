package com.example.karatemanagementsystem.service.impl;

import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.UserRepository;
import com.example.karatemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.ofNullable(userRepository.findById(id));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public Set<User> findUsersByTrainingSession(TrainingSession session) {
        return userRepository.findByTrainingSessions(session);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
