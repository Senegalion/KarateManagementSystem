package com.karate.payment_service.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private Long userId;
        private String userEmail;
        private String username;
        private Long clubId;
        private String clubName;
        private String karateRank;
        private LocalDate registrationDate;
    }
}