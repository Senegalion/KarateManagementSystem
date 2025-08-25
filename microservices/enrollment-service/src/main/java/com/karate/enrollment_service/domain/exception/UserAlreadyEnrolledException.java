package com.karate.enrollment_service.domain.exception;

public class UserAlreadyEnrolledException extends RuntimeException {
    public UserAlreadyEnrolledException(String message) {
        super(message);
    }
}
