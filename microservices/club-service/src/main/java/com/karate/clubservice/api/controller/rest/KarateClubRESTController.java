package com.karate.clubservice.api.controller.rest;

import com.karate.clubservice.domain.model.dto.KarateClubDto;
import com.karate.clubservice.domain.service.KarateClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/clubs")
@Tag(name = "Clubs", description = "Karate club information.")
public class KarateClubRESTController {
    private final KarateClubService clubService;

    @GetMapping("/by-name")
    @Operation(summary = "Get club by name")
    public KarateClubDto getClubByName(@RequestParam("name") String name) {
        log.info("GET /clubs/by-name name={}", name);

        KarateClubDto dto = clubService.getByName(name);

        log.info("200 /clubs/by-name name={} clubId={}", name, dto.karateClubId());
        return dto;
    }

    @GetMapping("/by-id/{id}")
    @Operation(summary = "Get club by ID")
    public KarateClubDto getClubById(@PathVariable Long id) {
        log.info("GET /clubs/by-id/{}", id);

        KarateClubDto dto = clubService.getById(id);

        log.info("200 /clubs/by-id/{} name={}", id, dto.name());
        return dto;
    }
}
