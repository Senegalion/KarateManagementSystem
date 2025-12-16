package com.karate.training_service.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record TrainingRecurringRequestDto(
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,

        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,

        @NotEmpty @Size(min = 1) Set<DayOfWeek> daysOfWeek,
        String description,

        boolean skipConflicts
) {
}
