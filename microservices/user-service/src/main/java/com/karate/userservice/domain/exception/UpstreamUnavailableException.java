package com.karate.userservice.domain.exception;

public class UpstreamUnavailableException extends RuntimeException {
    public UpstreamUnavailableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
