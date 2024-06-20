package com.example.karatemanagementsystem.controllers;

import com.example.karatemanagementsystem.model.Feedback;
import com.example.karatemanagementsystem.model.RoleName;
import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.FeedbackRepository;
import com.example.karatemanagementsystem.repository.TrainingSessionRepository;
import com.example.karatemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/admin")
public class AdminRESTController {

    private final UserRepository userRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final FeedbackRepository feedbackRepository;

    @Autowired
    public AdminRESTController(
            UserRepository userRepository,
            TrainingSessionRepository trainingSessionRepository,
            FeedbackRepository feedbackRepository
    ) {
        this.userRepository = userRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/by-role")
    public List<User> getUsersByRole() {
        return userRepository.findAll().stream()
                .sorted((user1, user2) -> {
                    boolean isAdmin1 = user1.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMIN));
                    boolean isAdmin2 = user2.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMIN));
                    return Boolean.compare(!isAdmin1, !isAdmin2);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/users/by-rank")
    public List<User> getUsersByRank() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getKarateRank).reversed())
                .collect(Collectors.toList());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        List<Feedback> feedbacks = feedbackRepository.findByUser(user);
        feedbackRepository.deleteAll(feedbacks);

        for (TrainingSession session : user.getTrainingSessions()) {
            session.getUsers().remove(user);
            trainingSessionRepository.save(session);
        }

        userRepository.delete(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/trainings")
    public List<TrainingSession> getAllTrainingSessions() {
        return trainingSessionRepository.findAll();
    }

    @PostMapping("/trainings")
    public ResponseEntity<TrainingSession> createTrainingSession(@RequestBody TrainingSession trainingSession) {
        trainingSessionRepository.save(trainingSession);
        return new ResponseEntity<>(trainingSession, HttpStatus.CREATED);
    }

    @DeleteMapping("/trainings/{id}")
    public ResponseEntity<?> deleteTrainingSession(@PathVariable("id") long id) {
        TrainingSession trainingSession = trainingSessionRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Training session not found"));

        List<Feedback> feedbacks = feedbackRepository.findByTrainingSession(trainingSession);
        feedbackRepository.deleteAll(feedbacks);

        trainingSessionRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/trainings/{id}")
    public ResponseEntity<TrainingSession> updateTrainingSession(@RequestBody TrainingSession trainingSession, @PathVariable("id") long id) {
        trainingSession.setId(id);
        trainingSessionRepository.save(trainingSession);
        return new ResponseEntity<>(trainingSession, HttpStatus.OK);
    }

    @GetMapping("/trainings/{sessionId}/members")
    public ResponseEntity<Set<User>> getTrainingSessionMembers(@PathVariable Long sessionId) {
        TrainingSession session = trainingSessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        Set<User> users = session.getUsers();
        if (users.isEmpty()) {
            users = userRepository.findByTrainingSessions(session);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/trainings/{sessionId}/feedback")
    public ResponseEntity<Void> addFeedback(@PathVariable Long sessionId, @RequestParam Long userId, @RequestBody Feedback feedback) {
        TrainingSession session = trainingSessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        feedback.setTrainingSession(session);
        feedback.setUser(user);
        feedbackRepository.save(feedback);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
