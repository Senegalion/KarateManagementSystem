package com.karate.clubservice.domain.service;

import com.karate.clubservice.domain.exception.ClubNotFoundException;
import com.karate.clubservice.domain.exception.InvalidClubNameException;
import com.karate.clubservice.domain.model.KarateClubEntity;
import com.karate.clubservice.domain.model.KarateClubName;
import com.karate.clubservice.domain.model.dto.KarateClubDto;
import com.karate.clubservice.domain.repository.KarateClubRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
@AllArgsConstructor
public class KarateClubService {
    private final KarateClubRepository repository;

    @Cacheable(cacheNames = "clubByName", key = "#name.toUpperCase()", unless = "#result == null")
    public KarateClubDto getByName(String name) {
        log.debug("Fetching club by name={}", name);
        KarateClubName clubName = parseClubName(name);

        KarateClubEntity entity = repository.findByName(clubName)
                .orElseThrow(() -> new ClubNotFoundException("Club not found: " + name));

        KarateClubDto dto = toDto(entity);
        log.debug("Fetched club by name={} -> id={}", name, dto.karateClubId());
        return dto;
    }

    @Cacheable(cacheNames = "clubById", key = "#id", unless = "#result == null")
    public KarateClubDto getById(Long id) {
        log.debug("Fetching club by id={}", id);
        KarateClubEntity entity = repository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException("Club not found with ID: " + id));

        KarateClubDto dto = toDto(entity);
        log.debug("Fetched club by id={} -> name={}", id, dto.name());
        return dto;
    }

    private KarateClubDto toDto(KarateClubEntity entity) {
        return KarateClubDto.builder()
                .karateClubId(entity.getKarateClubId())
                .name(entity.getName().name())
                .build();
    }

    private KarateClubName parseClubName(String name) {
        return Arrays.stream(KarateClubName.values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new InvalidClubNameException("Invalid club name: " + name));
    }
}
