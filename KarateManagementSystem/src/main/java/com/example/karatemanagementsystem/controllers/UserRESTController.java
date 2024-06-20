package com.example.karatemanagementsystem.controllers;

import com.example.karatemanagementsystem.model.Feedback;
import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.FeedbackRepository;
import com.example.karatemanagementsystem.repository.TrainingSessionRepository;
import com.example.karatemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/users")
public class UserRESTController {

    private final UserRepository userRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final FeedbackRepository feedbackRepository;

    @Autowired
    public UserRESTController(UserRepository userRepository, TrainingSessionRepository trainingSessionRepository, FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/trainings")
    public List<TrainingSession> getAvailableTrainingSessions() {
        return trainingSessionRepository.findAll();
    }

    @PostMapping("/trainings/{id}/signup")
    public ResponseEntity<?> signUpForTraining(@PathVariable("id") long trainingId) {
        User user = getCurrentUser();
        TrainingSession trainingSession = trainingSessionRepository.findById(trainingId).orElse(null);
        if (trainingSession == null) {
            return new ResponseEntity<>("Training session not found", HttpStatus.NOT_FOUND);
        }

        if (trainingSession.getUsers().stream().anyMatch(u -> u.getId().equals(user.getId()))) {
            return new ResponseEntity<>("User is already signed up for this training session", HttpStatus.BAD_REQUEST);
        }

        trainingSession.getUsers().add(user);
        user.getTrainingSessions().add(trainingSession);
        userRepository.save(user);
        trainingSessionRepository.save(trainingSession);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/trainings/{id}/withdraw")
    public ResponseEntity<?> withdrawFromTraining(@PathVariable("id") long trainingId) {
        User user = getCurrentUser();
        TrainingSession trainingSession = trainingSessionRepository.findById(trainingId).orElse(null);

        if (trainingSession == null) {
            return new ResponseEntity<>("Training session not found", HttpStatus.NOT_FOUND);
        }

        if (!trainingSession.getUsers().contains(user)) {
            return new ResponseEntity<>("User is not signed up for this training session", HttpStatus.BAD_REQUEST);
        }

        trainingSession.getUsers().remove(user);
        user.getTrainingSessions().remove(trainingSession);
        userRepository.save(user);
        trainingSessionRepository.save(trainingSession);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/trainings/{id}/feedback")
    public List<Feedback> getFeedbackForTraining(@PathVariable("id") long trainingId) {
        User currentUser = getCurrentUser();
        TrainingSession trainingSession = trainingSessionRepository.findById(trainingId).orElse(null);
        if (trainingSession == null) {
            throw new RuntimeException("Training session not found");
        }

        return feedbackRepository.findByTrainingSession(trainingSession).stream()
                .filter(feedback -> feedback.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/signed-up-sessions")
    public List<TrainingSession> getUserSignedUpSessions() {
        User user = getCurrentUser();
        return user.getTrainingSessions();
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserInfo() {
        User user = getCurrentUser();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Current user: " + user.getEmail());
        return user;
    }
}