package com.karate.training_service.domain.exception;

public class AuthenticationMissingException extends RuntimeException {
    public AuthenticationMissingException(String message) {
        super(message);
    }
}
