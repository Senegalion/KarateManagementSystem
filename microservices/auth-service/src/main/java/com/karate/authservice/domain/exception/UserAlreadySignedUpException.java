package com.karate.authservice.domain.exception;

public class UserAlreadySignedUpException extends RuntimeException {
    public UserAlreadySignedUpException(String message) {
        super(message);
    }
}
