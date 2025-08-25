package com.karate.enrollment_service.domain.exception;

public class UserNotEnrolledException extends RuntimeException {
    public UserNotEnrolledException(String message) {
        super(message);
    }
}
