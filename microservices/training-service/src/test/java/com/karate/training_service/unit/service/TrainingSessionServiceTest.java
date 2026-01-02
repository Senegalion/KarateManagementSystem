package com.karate.training_service.unit.service;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.exception.AuthenticationMissingException;
import com.karate.training_service.domain.exception.InvalidTrainingTimeRangeException;
import com.karate.training_service.domain.exception.TrainingSessionClubMismatchException;
import com.karate.training_service.domain.exception.TrainingSessionNotFoundException;
import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.domain.repository.TrainingSessionRepository;
import com.karate.training_service.domain.service.TrainingSessionService;
import com.karate.training_service.domain.service.UpstreamGateway;
import com.karate.training_service.infrastructure.messaging.TrainingEventProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceTest {

    @Mock
    TrainingSessionRepository repo;

    @Mock
    UpstreamGateway upstream;

    @Mock
    CacheManager cacheManager;

    @Mock
    TrainingEventProducer trainingEventProducer;

    @InjectMocks
    TrainingSessionService service;

    @BeforeEach
    void setUpAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john", "N/A")
        );
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllTrainingSessionsForCurrentUserClub_returnsMappedDtos() {
        // given
        when(upstream.getUserClubId("john")).thenReturn(42L);

        TrainingSessionEntity e1 = new TrainingSessionEntity();
        e1.setTrainingSessionId(1L);
        e1.setStartTime(LocalDateTime.now());
        e1.setEndTime(LocalDateTime.now().plusHours(1));
        e1.setDescription("a");
        e1.setClubId(42L);

        TrainingSessionEntity e2 = new TrainingSessionEntity();
        e2.setTrainingSessionId(2L);
        e2.setStartTime(LocalDateTime.now());
        e2.setEndTime(LocalDateTime.now().plusHours(2));
        e2.setDescription("b");
        e2.setClubId(42L);

        when(repo.findAllByClubId(42L)).thenReturn(List.of(e1, e2));

        // when
        List<TrainingSessionDto> out = service.getAllTrainingSessionsForCurrentUserClub();

        // then
        assertThat(out).hasSize(2);
        assertThat(out.get(0).description()).isEqualTo("a");
        verify(upstream).getUserClubId("john");
        verify(repo).findAllByClubId(42L);
    }

    @Test
    void getAllTrainingSessionsForCurrentUserClub_noAuth_throws401() {
        // given
        SecurityContextHolder.clearContext();

        // when && then
        assertThatThrownBy(() -> service.getAllTrainingSessionsForCurrentUserClub())
                .isInstanceOf(AuthenticationMissingException.class);

        verifyNoInteractions(upstream, repo);
    }

    @Test
    void createTrainingSession_ok_persistsWithClubId() {
        // given
        when(upstream.getUserClubId("john")).thenReturn(7L);
        LocalDateTime s = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime e = s.plusHours(1);
        TrainingSessionRequestDto in = new TrainingSessionRequestDto(s, e, "desc");

        TrainingSessionEntity saved = new TrainingSessionEntity();
        saved.setTrainingSessionId(99L);
        saved.setStartTime(s);
        saved.setEndTime(e);
        saved.setDescription("desc");
        saved.setClubId(7L);

        when(repo.save(any(TrainingSessionEntity.class))).thenReturn(saved);
        when(cacheManager.getCache(anyString())).thenReturn(mock(org.springframework.cache.Cache.class));

        // when
        TrainingSessionDto dto = service.createTrainingSession(in);

        // then
        assertThat(dto.trainingSessionId()).isEqualTo(99L);
        assertThat(dto.description()).isEqualTo("desc");
        verify(upstream).getUserClubId("john");
        ArgumentCaptor<TrainingSessionEntity> cap = ArgumentCaptor.forClass(TrainingSessionEntity.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getClubId()).isEqualTo(7L);
    }

    @Test
    void createTrainingSession_invalidRange_throws400() {
        // given
        LocalDateTime s = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime e = s; // equal -> invalid
        TrainingSessionRequestDto in = new TrainingSessionRequestDto(s, e, "x");

        // when && then
        assertThatThrownBy(() -> service.createTrainingSession(in))
                .isInstanceOf(InvalidTrainingTimeRangeException.class);

        verifyNoInteractions(upstream, repo);
    }

    @Test
    void deleteTrainingSession_ok_sameClub_deletes_and_sends_event() {
        // given
        when(upstream.getUserClubId("john")).thenReturn(10L);

        TrainingSessionEntity ent = new TrainingSessionEntity();
        ent.setTrainingSessionId(5L);
        ent.setClubId(10L);
        ent.setStartTime(LocalDateTime.now());
        ent.setEndTime(LocalDateTime.now().plusHours(1));
        ent.setDescription("d");

        when(repo.findById(5L)).thenReturn(Optional.of(ent));
        when(cacheManager.getCache(anyString())).thenReturn(mock(org.springframework.cache.Cache.class));

        // when
        service.deleteTrainingSession(5L);

        // then
        verify(repo).delete(ent);
        verify(trainingEventProducer).sendTrainingDeletedEvent(any());
    }

    @Test
    void deleteTrainingSession_notFound_throws404() {
        // given
        when(repo.findById(123L)).thenReturn(Optional.empty());

        // when && then
        assertThatThrownBy(() -> service.deleteTrainingSession(123L))
                .isInstanceOf(TrainingSessionNotFoundException.class);

        verify(repo, never()).delete(any());
    }

    @Test
    void deleteTrainingSession_clubMismatch_throws403() {
        // given
        when(upstream.getUserClubId("john")).thenReturn(2L);
        TrainingSessionEntity ent = new TrainingSessionEntity();
        ent.setTrainingSessionId(5L);
        ent.setClubId(1L);
        ent.setStartTime(LocalDateTime.now());
        ent.setEndTime(LocalDateTime.now().plusHours(1));
        ent.setDescription("d");
        when(repo.findById(5L)).thenReturn(Optional.of(ent));

        // when && then
        assertThatThrownBy(() -> service.deleteTrainingSession(5L))
                .isInstanceOf(TrainingSessionClubMismatchException.class);
        verify(repo, never()).delete(any());
    }

    @Test
    void checkTrainingExists_delegatesToRepo() {
        // given
        when(repo.existsById(9L)).thenReturn(true);

        // when && then
        assertThat(service.checkTrainingExists(9L)).isTrue();
        verify(repo).existsById(9L);
    }

    @Test
    void getTrainingById_ok_returnsDto() {
        // given
        TrainingSessionEntity ent = new TrainingSessionEntity();
        ent.setTrainingSessionId(11L);
        ent.setStartTime(LocalDateTime.now());
        ent.setEndTime(LocalDateTime.now().plusHours(1));
        ent.setDescription("abc");
        ent.setClubId(1L);
        when(repo.findById(11L)).thenReturn(Optional.of(ent));

        // when
        TrainingSessionDto dto = service.getTrainingById(11L);

        // then
        assertThat(dto.trainingSessionId()).isEqualTo(11L);
        assertThat(dto.description()).isEqualTo("abc");
    }

    @Test
    void getTrainingById_notFound_throwsRuntime() {
        // given
        when(repo.findById(404L)).thenReturn(Optional.empty());

        // when && then
        assertThatThrownBy(() -> service.getTrainingById(404L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Training Session not found");
    }

    @Test
    void deleteTrainingSession_endTimePresent_butPast_throws400() {
        when(upstream.getUserClubId("john")).thenReturn(10L);

        var ent = new TrainingSessionEntity();
        ent.setTrainingSessionId(5L);
        ent.setClubId(10L);
        ent.setStartTime(LocalDateTime.now().minusHours(2));
        ent.setEndTime(LocalDateTime.now().minusMinutes(1));
        ent.setDescription("d");

        when(repo.findById(5L)).thenReturn(Optional.of(ent));

        assertThatThrownBy(() -> service.deleteTrainingSession(5L))
                .isInstanceOf(com.karate.training_service.domain.exception.TrainingAlreadyOccurredException.class)
                .hasMessageContaining("already finished");

        verify(repo, never()).delete(any());
    }

    @Test
    void deleteTrainingSession_noEndTime_andStarted_throws400() {
        when(upstream.getUserClubId("john")).thenReturn(10L);

        var ent = new TrainingSessionEntity();
        ent.setTrainingSessionId(6L);
        ent.setClubId(10L);
        ent.setStartTime(LocalDateTime.now().minusMinutes(1));
        ent.setEndTime(null);
        ent.setDescription("d");

        when(repo.findById(6L)).thenReturn(Optional.of(ent));

        assertThatThrownBy(() -> service.deleteTrainingSession(6L))
                .isInstanceOf(com.karate.training_service.domain.exception.TrainingAlreadyOccurredException.class)
                .hasMessageContaining("already started");

        verify(repo, never()).delete(any());
    }

    @Test
    void createRecurringTrainings_toDateBeforeFromDate_throws() {
        var dto = new com.karate.training_service.api.dto.TrainingRecurringRequestDto(
                java.time.LocalDate.of(2025, 1, 10),
                java.time.LocalDate.of(2025, 1, 1),
                java.time.LocalTime.of(10, 0),
                java.time.LocalTime.of(11, 0),
                java.util.Set.of(java.time.DayOfWeek.MONDAY),
                "Description",
                false
        );

        assertThatThrownBy(() -> service.createRecurringTrainings(dto))
                .isInstanceOf(com.karate.training_service.domain.exception.InvalidTrainingTimeRangeException.class)
                .hasMessageContaining("toDate must be");

        verifyNoInteractions(upstream, repo);
    }

    @Test
    void createRecurringTrainings_endTimeNotAfterStart_throws() {
        var dto = new com.karate.training_service.api.dto.TrainingRecurringRequestDto(
                java.time.LocalDate.of(2025, 1, 1),
                java.time.LocalDate.of(2025, 1, 1),
                java.time.LocalTime.of(10, 0),
                java.time.LocalTime.of(10, 0),
                java.util.Set.of(java.time.DayOfWeek.WEDNESDAY),
                "Description",
                false
        );

        assertThatThrownBy(() -> service.createRecurringTrainings(dto))
                .isInstanceOf(com.karate.training_service.domain.exception.InvalidTrainingTimeRangeException.class)
                .hasMessageContaining("End time must be after");

        verifyNoInteractions(upstream, repo);
    }

    @Test
    void createRecurringTrainings_conflict_andNoSkip_throws() {
        when(upstream.getUserClubId("john")).thenReturn(7L);

        var dto = new com.karate.training_service.api.dto.TrainingRecurringRequestDto(
                java.time.LocalDate.of(2025, 1, 6),  // Monday
                java.time.LocalDate.of(2025, 1, 6),
                java.time.LocalTime.of(10, 0),
                java.time.LocalTime.of(11, 0),
                java.util.Set.of(java.time.DayOfWeek.MONDAY),
                "Description",
                false
        );

        var startDT = java.time.LocalDateTime.of(2025, 1, 6, 10, 0);
        when(repo.existsByClubIdAndStartTime(7L, startDT)).thenReturn(true);

        assertThatThrownBy(() -> service.createRecurringTrainings(dto))
                .isInstanceOf(com.karate.training_service.domain.exception.TrainingConflictException.class)
                .hasMessageContaining("Training already exists");

        verify(repo, never()).saveAll(any());
    }

    @Test
    void createRecurringTrainings_conflict_andSkip_savesOnlyNonConflicting() {
        when(upstream.getUserClubId("john")).thenReturn(7L);
        when(cacheManager.getCache(anyString())).thenReturn(mock(org.springframework.cache.Cache.class));

        var dto = new com.karate.training_service.api.dto.TrainingRecurringRequestDto(
                java.time.LocalDate.of(2025, 1, 6),
                java.time.LocalDate.of(2025, 1, 8),
                java.time.LocalTime.of(10, 0),
                java.time.LocalTime.of(11, 0),
                java.util.Set.of(java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.WEDNESDAY),
                "rec",
                true
        );

        var monStart = java.time.LocalDateTime.of(2025, 1, 6, 10, 0);
        var wedStart = java.time.LocalDateTime.of(2025, 1, 8, 10, 0);

        when(repo.existsByClubIdAndStartTime(7L, monStart)).thenReturn(true);
        when(repo.existsByClubIdAndStartTime(7L, wedStart)).thenReturn(false);

        var savedEnt = new TrainingSessionEntity();
        savedEnt.setTrainingSessionId(1L);
        savedEnt.setClubId(7L);
        savedEnt.setStartTime(wedStart);
        savedEnt.setEndTime(java.time.LocalDateTime.of(2025, 1, 8, 11, 0));
        savedEnt.setDescription("rec");

        when(repo.saveAll(anyList())).thenReturn(List.of(savedEnt));

        var out = service.createRecurringTrainings(dto);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).startTime()).isEqualTo(wedStart);

        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(captor.capture());
        assertThat((List<?>) captor.getValue()).hasSize(1);
    }

    @Test
    void createTrainingSession_evictsAllCaches_whenCachePresent() {
        when(upstream.getUserClubId("john")).thenReturn(7L);

        var cacheTrainingsByClub = mock(org.springframework.cache.Cache.class);
        var cacheById = mock(org.springframework.cache.Cache.class);
        var cacheExists = mock(org.springframework.cache.Cache.class);

        when(cacheManager.getCache("trainingsByClub")).thenReturn(cacheTrainingsByClub);
        when(cacheManager.getCache("trainingById")).thenReturn(cacheById);
        when(cacheManager.getCache("trainingExists")).thenReturn(cacheExists);

        var s = LocalDateTime.of(2025,1,1,10,0);
        var e = s.plusHours(1);
        when(repo.save(any())).thenAnswer(inv -> {
            var ent = (TrainingSessionEntity) inv.getArgument(0);
            ent.setTrainingSessionId(99L);
            return ent;
        });

        service.createTrainingSession(new TrainingSessionRequestDto(s, e, "x"));

        verify(cacheTrainingsByClub).evictIfPresent(7L);
        verify(cacheById).evictIfPresent(99L);
        verify(cacheExists).evictIfPresent(99L);
    }

    @Test
    void createTrainingSession_doesNotFail_whenCacheMissing() {
        when(upstream.getUserClubId("john")).thenReturn(7L);
        when(cacheManager.getCache(anyString())).thenReturn(null);

        var s = LocalDateTime.of(2025,1,1,10,0);
        var e = s.plusHours(1);
        when(repo.save(any())).thenAnswer(inv -> {
            var ent = (TrainingSessionEntity) inv.getArgument(0);
            ent.setTrainingSessionId(99L);
            return ent;
        });

        service.createTrainingSession(new TrainingSessionRequestDto(s, e, "x"));
    }
}
