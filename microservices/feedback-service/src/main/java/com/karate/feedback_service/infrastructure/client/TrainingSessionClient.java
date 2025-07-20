package com.karate.feedback_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "training-service")
public interface TrainingSessionClient {
    @GetMapping("/training-sessions/{sessionId}/exists")
    Boolean checkTrainingSessionExists(@PathVariable Long sessionId);
}
