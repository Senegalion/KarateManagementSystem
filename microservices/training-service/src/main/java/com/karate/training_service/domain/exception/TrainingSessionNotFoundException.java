package com.karate.training_service.domain.exception;

public class TrainingSessionNotFoundException extends RuntimeException {
    public TrainingSessionNotFoundException(String message) {
        super(message);
    }
}
