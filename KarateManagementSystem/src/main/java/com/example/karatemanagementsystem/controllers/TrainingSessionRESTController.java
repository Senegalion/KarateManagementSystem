package com.example.karatemanagementsystem.controllers;

import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.repository.TrainingSessionRepository;
import com.example.karatemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/trainings")
public class TrainingSessionRESTController {

    private final TrainingSessionRepository trainingSessionRepository;
    private final UserRepository userRepository;

    @Autowired
    public TrainingSessionRESTController(
            TrainingSessionRepository trainingSessionRepository,
            UserRepository userRepository
    ) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<TrainingSession> getAllTrainingSessions() {
        return trainingSessionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingSession> getTrainingSessionById(@PathVariable("id") long id) {
        Optional<TrainingSession> trainingSession = trainingSessionRepository.findById(id);
        if (trainingSession.isPresent()) {
            return new ResponseEntity<>(trainingSession.get(), HttpStatus.OK);
        } else {
            System.out.println("Training session not found!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<TrainingSession> createTrainingSession(@RequestBody TrainingSession trainingSession) {
        trainingSessionRepository.save(trainingSession);
        return new ResponseEntity<>(trainingSession, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingSession> updateTrainingSession(@RequestBody TrainingSession trainingSession, @PathVariable("id") long id) {
        trainingSession.setId(id);
        trainingSessionRepository.save(trainingSession);
        return new ResponseEntity<>(trainingSession, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<TrainingSession> deleteTrainingSessions() {
        trainingSessionRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrainingSession(@PathVariable("id") long id) {
        Optional<TrainingSession> trainingSession = trainingSessionRepository.findById(id);
        if (trainingSession.isPresent()) {
            trainingSessionRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            System.out.println("Training session not found!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
