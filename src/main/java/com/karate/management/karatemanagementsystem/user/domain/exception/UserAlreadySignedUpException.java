package com.karate.management.karatemanagementsystem.user.domain.exception;

public class UserAlreadySignedUpException extends RuntimeException {
    public UserAlreadySignedUpException(String message) {
        super(message);
    }
}
