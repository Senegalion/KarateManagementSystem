package com.karate.management.karatemanagementsystem.user.domain.exception;

public class UserNotSignedUpException extends RuntimeException {
    public UserNotSignedUpException(String message) {
        super(message);
    }
}
