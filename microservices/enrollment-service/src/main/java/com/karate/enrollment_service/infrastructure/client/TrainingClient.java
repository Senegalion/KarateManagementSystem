package com.karate.enrollment_service.infrastructure.client;

import com.karate.enrollment_service.infrastructure.client.config.FeignClientConfig;
import com.karate.enrollment_service.infrastructure.client.dto.TrainingSessionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "training-service", configuration = FeignClientConfig.class)
public interface TrainingClient {
    @GetMapping("/internal/trainings/{trainingId}/exists")
    Boolean checkTrainingSessionExists(@PathVariable Long trainingId);

    @GetMapping("/internal/trainings/{id}")
    TrainingSessionDto getTrainingById(@PathVariable("id") Long trainingId);
}
