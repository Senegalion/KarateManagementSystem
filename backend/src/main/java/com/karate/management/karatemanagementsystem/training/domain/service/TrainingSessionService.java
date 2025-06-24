package com.karate.management.karatemanagementsystem.training.domain.service;

import com.karate.management.karatemanagementsystem.training.api.dto.TrainingSessionRequestDto;
import com.karate.management.karatemanagementsystem.training.domain.exception.TrainingSessionClubMismatchException;
import com.karate.management.karatemanagementsystem.training.infrastructure.persistence.mapper.TrainingSessionMapper;
import com.karate.management.karatemanagementsystem.training.domain.exception.TrainingSessionNotFoundException;
import com.karate.management.karatemanagementsystem.training.domain.model.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.training.domain.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.training.api.dto.TrainingSessionRegistrationResponseDto;
import com.karate.management.karatemanagementsystem.training.api.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.user.domain.model.UserEntity;
import com.karate.management.karatemanagementsystem.user.domain.repository.UserRepository;
import com.karate.management.karatemanagementsystem.user.domain.exception.UserAlreadySignedUpException;
import com.karate.management.karatemanagementsystem.user.domain.exception.UserNotSignedUpException;
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

    public List<TrainingSessionDto> getAllTrainingSessionsForCurrentUserClub() {
        UserEntity user = getCurrentUser();
        Long clubId = user.getKarateClub().getKarateClubId();
        List<TrainingSessionEntity> trainingSessions = trainingSessionRepository.findAllByKarateClubKarateClubId(clubId);
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
    public TrainingSessionDto createTrainingSession(TrainingSessionRequestDto dto) {
        UserEntity user = getCurrentUser();

        TrainingSessionEntity trainingSession = new TrainingSessionEntity();
        trainingSession.setDate(dto.date());
        trainingSession.setDescription(dto.description());

        trainingSession.setKarateClub(user.getKarateClub());

        TrainingSessionEntity saved = trainingSessionRepository.save(trainingSession);

        return TrainingSessionDto.builder()
                .trainingSessionId(saved.getTrainingSessionId())
                .date(saved.getDate())
                .description(saved.getDescription())
                .build();
    }

    @Transactional
    public TrainingSessionRegistrationResponseDto signUpForTrainingSession(Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TrainingSessionEntity session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training session not found"));

        if (!session.getKarateClub().getKarateClubId().equals(user.getKarateClub().getKarateClubId())) {
            throw new TrainingSessionClubMismatchException("You can't sign up for a session outside your club.");
        }

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

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
