package com.karate.clubservice.api.controller.rest;

import com.karate.clubservice.domain.model.dto.KarateClubDto;
import com.karate.clubservice.domain.service.KarateClubService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/clubs")
public class KarateClubRESTController {
    private final KarateClubService clubService;

    @GetMapping("/by-name")
    public KarateClubDto getClubByName(@RequestParam("name") String name) {
        return clubService.getByName(name);
    }

    @GetMapping("/by-id/{id}")
    public KarateClubDto getClubById(@PathVariable Long id) {
        return clubService.getById(id);
    }
}
