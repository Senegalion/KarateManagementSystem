package com.karate.authservice.domain.exception;

public class UserNotSignedUpException extends RuntimeException {
    public UserNotSignedUpException(String message) {
        super(message);
    }
}
