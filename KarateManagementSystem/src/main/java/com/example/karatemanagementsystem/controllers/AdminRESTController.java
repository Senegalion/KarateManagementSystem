package com.example.karatemanagementsystem.controllers;

import com.example.karatemanagementsystem.model.Feedback;
import com.example.karatemanagementsystem.model.RoleName;
import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.service.FeedbackService;
import com.example.karatemanagementsystem.service.TrainingSessionService;
import com.example.karatemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/admin")
public class AdminRESTController {

    private final UserService userService;
    private final TrainingSessionService trainingSessionService;
    private final FeedbackService feedbackService;

    @Autowired
    public AdminRESTController(UserService userService, TrainingSessionService trainingSessionService, FeedbackService feedbackService) {
        this.userService = userService;
        this.trainingSessionService = trainingSessionService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/by-role")
    public List<User> getUsersByRole() {
        return userService.getAllUsers().stream()
                .sorted((user1, user2) -> {
                    boolean isAdmin1 = user1.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMIN));
                    boolean isAdmin2 = user2.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMIN));
                    return Boolean.compare(!isAdmin1, !isAdmin2);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/users/by-rank")
    public List<User> getUsersByRank() {
        return userService.getAllUsers().stream()
                .sorted(Comparator.comparing(User::getKarateRank).reversed())
                .collect(Collectors.toList());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found"));

        List<Feedback> feedbacks = feedbackService.getFeedbackByUser(user);
        feedbackService.deleteAllFeedbacks(feedbacks);

        for (TrainingSession session : user.getTrainingSessions()) {
            session.getUsers().remove(user);
            trainingSessionService.saveTrainingSession(session);
        }

        userService.deleteUser(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/trainings")
    public List<TrainingSession> getAllTrainingSessions() {
        return trainingSessionService.getAllTrainingSessions();
    }

    @PostMapping("/trainings")
    public ResponseEntity<TrainingSession> createTrainingSession(@RequestBody TrainingSession trainingSession) {
        trainingSessionService.saveTrainingSession(trainingSession);
        return new ResponseEntity<>(trainingSession, HttpStatus.CREATED);
    }

    @DeleteMapping("/trainings/{id}")
    public ResponseEntity<?> deleteTrainingSession(@PathVariable("id") long id) {
        TrainingSession trainingSession = trainingSessionService
                .getTrainingSessionById(id)
                .orElseThrow(() -> new RuntimeException("Training session not found"));

        List<Feedback> feedbacks = feedbackService.getFeedbackByTrainingSession(trainingSession);
        feedbackService.deleteAllFeedbacks(feedbacks);

        trainingSessionService.deleteTrainingSession(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/trainings/{id}")
    public ResponseEntity<TrainingSession> updateTrainingSession(@RequestBody TrainingSession trainingSession, @PathVariable("id") long id) {
        trainingSession.setId(id);
        trainingSessionService.saveTrainingSession(trainingSession);
        return new ResponseEntity<>(trainingSession, HttpStatus.OK);
    }

    @GetMapping("/trainings/{sessionId}/members")
    public ResponseEntity<Set<User>> getTrainingSessionMembers(@PathVariable Long sessionId) {
        TrainingSession session = trainingSessionService.getTrainingSessionById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        Set<User> users = session.getUsers();
        if (users.isEmpty()) {
            users = userService.findUsersByTrainingSession(session);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/trainings/{sessionId}/feedback")
    public ResponseEntity<Void> addFeedback(@PathVariable Long sessionId, @RequestParam Long userId, @RequestBody Feedback feedback) {
        TrainingSession session = trainingSessionService.getTrainingSessionById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        User user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        feedback.setTrainingSession(session);
        feedback.setUser(user);
        feedbackService.saveFeedback(feedback);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
