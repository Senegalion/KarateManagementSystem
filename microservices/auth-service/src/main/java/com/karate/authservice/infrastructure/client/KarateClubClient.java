package com.karate.authservice.infrastructure.client;

import com.karate.authservice.infrastructure.client.config.FeignClientConfig;
import com.karate.authservice.infrastructure.client.dto.KarateClubDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "club-service", configuration = FeignClientConfig.class)
public interface KarateClubClient {
    @GetMapping("/clubs/by-name")
    KarateClubDto getClubByName(@RequestParam("name") String clubName);

    @GetMapping("/clubs/by-id/{id}")
    KarateClubDto getClubById(@PathVariable("id") Long karateClubId);
}
