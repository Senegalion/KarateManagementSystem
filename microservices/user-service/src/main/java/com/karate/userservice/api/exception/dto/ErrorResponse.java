package com.karate.userservice.api.exception.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String message,
        List<ValidationError> errors,
        String path,
        LocalDateTime timestamp
) {
}
