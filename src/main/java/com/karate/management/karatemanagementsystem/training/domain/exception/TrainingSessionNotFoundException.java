package com.karate.management.karatemanagementsystem.training.domain.exception;

public class TrainingSessionNotFoundException extends RuntimeException {
    public TrainingSessionNotFoundException(String message) {
        super(message);
    }
}
