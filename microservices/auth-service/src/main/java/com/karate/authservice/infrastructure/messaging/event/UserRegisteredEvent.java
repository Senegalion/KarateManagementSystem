package com.karate.authservice.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {
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

        private Long clubId;
        private String clubName;
        private String karateRank;
    }
}

