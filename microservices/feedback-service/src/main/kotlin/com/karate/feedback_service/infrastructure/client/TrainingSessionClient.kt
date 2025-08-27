package com.karate.feedback_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "training-service")
interface TrainingSessionClient {
    @GetMapping("/internal/trainings/{trainingId}/exists")
    fun checkTrainingExists(@PathVariable trainingId: Long): Boolean?
}
