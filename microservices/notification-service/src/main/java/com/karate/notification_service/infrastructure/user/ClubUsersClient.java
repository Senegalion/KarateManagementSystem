package com.karate.notification_service.infrastructure.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubUsersClient {

    private final RestTemplate restTemplate;

    @Value("${app.userService.baseUrl}")
    private String baseUrl;

    public List<String> getClubUserEmails(Long clubId) {
        String url = baseUrl + "/internal/clubs/" + clubId + "/users/emails";
        String[] arr = restTemplate.getForObject(url, String[].class);
        return arr == null ? List.of() : List.of(arr);
    }
}
