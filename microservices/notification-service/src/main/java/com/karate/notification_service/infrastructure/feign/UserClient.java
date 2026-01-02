package com.karate.notification_service.infrastructure.feign;

import com.karate.notification_service.infrastructure.feign.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserClient {

    @GetMapping("/internal/users/{clubId}/users/emails")
    List<String> getClubUserEmails(@PathVariable("clubId") Long clubId);
}
