package com.karate.notification_service.infrastructure.feign;

import com.karate.notification_service.infrastructure.feign.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "enrollment-service", configuration = FeignClientConfig.class)
public interface EnrollmentClient {

    @GetMapping("/internal/enrollments/training/{trainingId}/emails")
    List<String> getEnrolledEmails(@PathVariable("trainingId") Long trainingId);
}
