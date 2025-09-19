package com.karate.training_service.domain.exception;

public class UpstreamUnavailableException extends RuntimeException {
    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
