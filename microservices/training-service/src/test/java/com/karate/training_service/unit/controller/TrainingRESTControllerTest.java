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
}
