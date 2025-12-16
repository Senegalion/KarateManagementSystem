package com.karate.training_service.domain.exception;

public class TrainingConflictException extends RuntimeException {
    public TrainingConflictException(String message) {
        super(message);
    }
}
