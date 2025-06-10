package com.karate.management.karatemanagementsystem.domain.user;

public class UserNotSignedUpException extends RuntimeException {
    public UserNotSignedUpException(String message) {
        super(message);
    }
}
