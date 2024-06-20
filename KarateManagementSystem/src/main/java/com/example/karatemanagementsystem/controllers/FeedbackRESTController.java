package com.example.karatemanagementsystem.controllers;

import com.example.karatemanagementsystem.model.Feedback;
import com.example.karatemanagementsystem.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/feedbacks")
public class FeedbackRESTController {

    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackRESTController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable("id") long id) {
        Feedback feedback = feedbackRepository.findById(id);
        if (feedback == null) {
            System.out.println("Feedback not found!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback) {
        feedbackRepository.save(feedback);
        return new ResponseEntity<>(feedback, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feedback> updateFeedback(@RequestBody Feedback feedback, @PathVariable("id") long id) {
        feedback.setId(id);
        feedbackRepository.save(feedback);
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Feedback> deleteFeedback() {
        feedbackRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable("id") long id) {
        Feedback feedback = feedbackRepository.findById(id);
        if (feedback == null) {
            System.out.println("Feedback not found!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        feedbackRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
