package com.karate.training_service.domain.exception;

public class InvalidTrainingTimeRangeException extends RuntimeException {
    public InvalidTrainingTimeRangeException(String message) {
        super(message);
    }
}
