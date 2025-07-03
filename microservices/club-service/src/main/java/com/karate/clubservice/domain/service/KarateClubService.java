package com.karate.clubservice.domain.service;

import com.karate.clubservice.domain.model.KarateClubEntity;
import com.karate.clubservice.domain.model.KarateClubName;
import com.karate.clubservice.domain.model.dto.KarateClubDto;
import com.karate.clubservice.domain.repository.KarateClubRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@AllArgsConstructor
public class KarateClubService {
    private final KarateClubRepository repository;

    public KarateClubDto getByName(String name) {
        KarateClubName clubName = parseClubName(name);

        KarateClubEntity entity = repository.findByName(clubName)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + name));
        return toDto(entity);
    }

    public KarateClubDto getById(Long id) {
        KarateClubEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found with ID: " + id));
        return toDto(entity);
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
                .orElseThrow(() -> new IllegalArgumentException("Invalid club name: " + name));
    }
}
