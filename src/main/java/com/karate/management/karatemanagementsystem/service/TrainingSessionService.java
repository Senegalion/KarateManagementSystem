package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.controller.rest.user.TrainingSessionRegistrationResponseDto;
import com.karate.management.karatemanagementsystem.model.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.service.exception.TrainingSessionNotFoundException;
import com.karate.management.karatemanagementsystem.service.exception.UserAlreadySignedUpException;
import com.karate.management.karatemanagementsystem.service.mapper.TrainingSessionMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class TrainingSessionService {
    public static final String SUCCESSFULLY_SIGNED_UP_FOR_THE_TRAINING_SESSION = "Successfully signed up for the training session";
    private final TrainingSessionRepository trainingSessionRepository;
    private final UserRepository userRepository;

    public List<TrainingSessionDto> getAllTrainingSessions() {
        List<TrainingSessionEntity> trainingSessions = trainingSessionRepository.findAll();
        if (trainingSessions.isEmpty()) {
            throw new TrainingSessionNotFoundException("No training sessions found");
        }
        return trainingSessions.stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .toList();
    }

    @Transactional
    public TrainingSessionRegistrationResponseDto signUpForTrainingSession(Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TrainingSessionEntity session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training session not found"));

        if (user.getTrainingSessionEntities().contains(session)) {
            throw new UserAlreadySignedUpException("User is already signed up for this session");
        }

        user.getTrainingSessionEntities().add(session);
        session.getUserEntities().add(user);

        userRepository.save(user);
        trainingSessionRepository.save(session);

        LocalDateTime truncatedDate = session.getDate().truncatedTo(ChronoUnit.MINUTES);

        return TrainingSessionRegistrationResponseDto.builder()
                .message(SUCCESSFULLY_SIGNED_UP_FOR_THE_TRAINING_SESSION)
                .date(truncatedDate)
                .description(session.getDescription())
                .build();
    }
}
