package com.karate.training_service.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.training_service.api.controller.rest.TrainingRESTController;
import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.api.exception.GlobalExceptionHandler;
import com.karate.training_service.domain.exception.InvalidTrainingTimeRangeException;
import com.karate.training_service.domain.exception.TrainingSessionClubMismatchException;
import com.karate.training_service.domain.exception.TrainingSessionNotFoundException;
import com.karate.training_service.domain.service.TrainingSessionService;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainingRESTController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TrainingRESTControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    TrainingSessionService service;

    @Test
    @WithMockUser(roles = "USER")
    void getAll_ok_forUserRole() throws Exception {
        TrainingSessionDto dto = TrainingSessionDto.builder()
                .trainingSessionId(1L)
                .startTime(LocalDateTime.parse("2025-01-01T10:00:00"))
                .endTime(LocalDateTime.parse("2025-01-01T11:00:00"))
                .description("x")
                .build();
        when(service.getAllTrainingSessionsForCurrentUserClub()).thenReturn(List.of(dto));

        mvc.perform(get("/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trainingSessionId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_created_201() throws Exception {
        var req = new TrainingSessionRequestDto(
                LocalDateTime.parse("2025-01-01T10:00:00"),
                LocalDateTime.parse("2025-01-01T11:00:00"),
                "ok"
        );
        var resp = TrainingSessionDto.builder()
                .trainingSessionId(99L)
                .startTime(req.startTime())
                .endTime(req.endTime())
                .description(req.description())
                .build();

        when(service.createTrainingSession(any())).thenReturn(resp);

        mvc.perform(post("/trainings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trainingSessionId").value(99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_validation_400_whenMissingFields() throws Exception {
        // brak wszystkich pól:
        mvc.perform(post("/trainings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.errors", hasSize(3)))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("startTime", "endTime", "description")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_malformedBody_400() throws Exception {
        mvc.perform(post("/trainings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf("not-a-json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Request body is missing or malformed")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_serviceThrowsInvalidRange_mapsTo400() throws Exception {
        when(service.createTrainingSession(any()))
                .thenThrow(new InvalidTrainingTimeRangeException("End time must be after start time"));

        var req = new TrainingSessionRequestDto(
                LocalDateTime.parse("2025-01-01T12:00:00"),
                LocalDateTime.parse("2025-01-01T11:00:00"),
                "x"
        );

        mvc.perform(post("/trainings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("End time must be after start time")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_noContent_204() throws Exception {
        doNothing().when(service).deleteTrainingSession(5L);

        mvc.perform(delete("/trainings/{id}", 5L))
                .andExpect(status().isNoContent());

        Mockito.verify(service).deleteTrainingSession(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_notFound_404() throws Exception {
        Mockito.doThrow(new TrainingSessionNotFoundException("Training session not found"))
                .when(service).deleteTrainingSession(404L);

        mvc.perform(delete("/trainings/{id}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Training session not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_forbidden_403_whenClubMismatch() throws Exception {
        Mockito.doThrow(new TrainingSessionClubMismatchException("You cannot delete a training from another club"))
                .when(service).deleteTrainingSession(7L);

        mvc.perform(delete("/trainings/{id}", 7L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("another club")));
    }

    @Test
    void getAll_whenAuthMissingException_returns401() throws Exception {
        when(service.getAllTrainingSessionsForCurrentUserClub())
                .thenThrow(new com.karate.training_service.domain.exception.AuthenticationMissingException("No authenticated user found"));

        mvc.perform(get("/trainings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAll_whenServiceThrowsNoSuchElement_returns404_fromHandleNotFound() throws Exception {
        when(service.getAllTrainingSessionsForCurrentUserClub())
                .thenThrow(new java.util.NoSuchElementException("missing"));

        mvc.perform(get("/trainings"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("missing"))
                .andExpect(jsonPath("$.path").value("/trainings"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAll_whenServiceThrowsEntityNotFound_returns404_fromHandleNotFound() throws Exception {
        when(service.getAllTrainingSessionsForCurrentUserClub())
                .thenThrow(new jakarta.persistence.EntityNotFoundException("gone"));

        mvc.perform(get("/trainings"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("gone"))
                .andExpect(jsonPath("$.path").value("/trainings"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_whenServiceThrowsIllegalState_returns409_fromHandleConflict() throws Exception {
        when(service.createTrainingSession(any()))
                .thenThrow(new IllegalStateException("conflict"));

        var req = new TrainingSessionRequestDto(
                LocalDateTime.parse("2025-01-01T10:00:00"),
                LocalDateTime.parse("2025-01-01T11:00:00"),
                "x"
        );

        mvc.perform(post("/trainings/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("conflict"))
                .andExpect(jsonPath("$.path").value("/trainings/create"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAll_whenServiceThrowsFeignBadRequest_returns400_fromHandleFeignClientException() throws Exception {
        var req = feign.Request.create(
                feign.Request.HttpMethod.GET,
                "http://user-service/internal/users/x",
                java.util.Map.of(),
                null,
                feign.Util.UTF_8,
                null
        );

        FeignException.FeignClientException ex =
                new feign.FeignException.BadRequest("bad upstream", req, null, java.util.Map.of());

        when(service.getAllTrainingSessionsForCurrentUserClub()).thenThrow(ex);

        mvc.perform(get("/trainings"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Upstream service error:")))
                .andExpect(jsonPath("$.path").value("/trainings"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAll_whenServiceThrowsGeneric_returns500_fromHandleGenericException() throws Exception {
        when(service.getAllTrainingSessionsForCurrentUserClub())
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/trainings"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message", containsString("Unexpected error: boom")))
                .andExpect(jsonPath("$.path").value("/trainings"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAll_whenServiceThrowsUpstreamUnavailable_returns503() throws Exception {
        when(service.getAllTrainingSessionsForCurrentUserClub())
                .thenThrow(new com.karate.training_service.domain.exception.UpstreamUnavailableException(
                        "user-service unavailable", new RuntimeException("x")
                ));

        mvc.perform(get("/trainings"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("user-service unavailable"))
                .andExpect(jsonPath("$.path").value("/trainings"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRecurring_created201() throws Exception {
        var dto = new com.karate.training_service.api.dto.TrainingRecurringRequestDto(
                java.time.LocalDate.of(2025, 1, 1),
                java.time.LocalDate.of(2025, 1, 7),
                java.time.LocalTime.of(10, 0),
                java.time.LocalTime.of(11, 0),
                java.util.Set.of(java.time.DayOfWeek.MONDAY),
                "rec",
                true
        );

        when(service.createRecurringTrainings(any())).thenReturn(List.of(
                TrainingSessionDto.builder()
                        .trainingSessionId(1L)
                        .startTime(java.time.LocalDateTime.of(2025,1,6,10,0))
                        .endTime(java.time.LocalDateTime.of(2025,1,6,11,0))
                        .description("rec")
                        .build()
        ));

        mvc.perform(post("/trainings/create/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trainingSessionId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRecurring_validation400_whenMissingFields() throws Exception {
        mvc.perform(post("/trainings/create/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Validation failed")));
    }
}
