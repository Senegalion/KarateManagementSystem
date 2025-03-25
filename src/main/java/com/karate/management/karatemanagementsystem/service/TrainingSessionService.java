package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.controller.rest.user.TrainingSessionRegistrationResponseDto;
import com.karate.management.karatemanagementsystem.model.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.service.exception.TrainingSessionNotFoundException;
import com.karate.management.karatemanagementsystem.service.exception.UserAlreadySignedUpException;
import com.karate.management.karatemanagementsystem.service.exception.UserNotSignedUpException;
import com.karate.management.karatemanagementsystem.service.mapper.TrainingSessionMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TrainingSessionService {
    public static final String SUCCESSFULLY_SIGNED_UP_FOR_THE_TRAINING_SESSION = "Successfully signed up for the training session";
    public static final String SUCCESSFULLY_WITHDRAWN_FROM_THE_TRAINING_SESSION = "Successfully withdrawn from the training session";
    private final TrainingSessionRepository trainingSessionRepository;
    private final UserRepository userRepository;

    public List<TrainingSessionDto> getAllTrainingSessions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not found");
        }
        List<TrainingSessionEntity> trainingSessions = trainingSessionRepository.findAll();
        if (trainingSessions.isEmpty()) {
            throw new TrainingSessionNotFoundException("No training sessions found");
        }
        return trainingSessions.stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .toList();
    }

    @Transactional
    public List<TrainingSessionDto> getUserTrainingSessions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getTrainingSessionEntities().isEmpty()) {
            throw new TrainingSessionNotFoundException("No training sessions found");
        }
        return user.getTrainingSessionEntities().stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .collect(Collectors.toList());
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

        return TrainingSessionRegistrationResponseDto.builder()
                .message(SUCCESSFULLY_SIGNED_UP_FOR_THE_TRAINING_SESSION)
                .date(session.getDate().truncatedTo(ChronoUnit.MINUTES))
                .description(session.getDescription())
                .build();
    }

    @Transactional
    public TrainingSessionRegistrationResponseDto withdrawFromTrainingSession(Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TrainingSessionEntity session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training session not found"));

        if (!user.getTrainingSessionEntities().contains(session)) {
            throw new UserNotSignedUpException("User is not signed up for this session");
        }

        user.getTrainingSessionEntities().remove(session);
        session.getUserEntities().remove(user);

        userRepository.save(user);
        trainingSessionRepository.save(session);

        return TrainingSessionRegistrationResponseDto.builder()
                .message(SUCCESSFULLY_WITHDRAWN_FROM_THE_TRAINING_SESSION)
                .date(session.getDate().truncatedTo(ChronoUnit.MINUTES))
                .description(session.getDescription())
                .build();
    }
}
