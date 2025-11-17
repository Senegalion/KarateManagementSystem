package com.karate.training_service.domain.exception;

public class TrainingAlreadyOccurredException extends RuntimeException {
    public TrainingAlreadyOccurredException(String message) {
        super(message);
    }
}
