package com.karate.enrollment_service.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private Payload payload;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload {
        private Long userId;
        private String userEmail;
        private String username;

        private Long trainingId;
        private String trainingDescription;
        private LocalDateTime trainingStart;
        private LocalDateTime trainingEnd;
    }
}

