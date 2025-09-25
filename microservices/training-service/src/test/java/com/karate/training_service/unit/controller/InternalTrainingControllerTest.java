package com.karate.training_service.unit.controller;

import com.karate.training_service.api.controller.rest.InternalTrainingController;
import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.exception.GlobalExceptionHandler;
import com.karate.training_service.domain.exception.TrainingSessionNotFoundException;
import com.karate.training_service.domain.service.TrainingSessionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InternalTrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InternalTrainingControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    TrainingSessionService service;

    @Test
    void exists_true() throws Exception {
        // given
        when(service.checkTrainingExists(5L)).thenReturn(true);

        // when && then
        mvc.perform(get("/internal/trainings/{id}/exists", 5L))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void exists_false() throws Exception {
        // given
        when(service.checkTrainingExists(5L)).thenReturn(false);

        // when && then
        mvc.perform(get("/internal/trainings/{id}/exists", 5L))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getById_ok() throws Exception {
        // given
        var dto = TrainingSessionDto.builder()
                .trainingSessionId(7L)
                .startTime(LocalDateTime.parse("2025-01-01T10:00:00"))
                .endTime(LocalDateTime.parse("2025-01-01T11:00:00"))
                .description("d")
                .build();

        when(service.getTrainingById(7L)).thenReturn(dto);

        // when && then
        mvc.perform(get("/internal/trainings/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingSessionId").value(7));
    }

    @Test
    void getById_notFound_404() throws Exception {
        // given
        Mockito.reset(service);
        when(service.getTrainingById(anyLong())).thenThrow(new TrainingSessionNotFoundException("Training session not found"));

        // when && then
        mvc.perform(get("/internal/trainings/{id}", 123L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }
}
