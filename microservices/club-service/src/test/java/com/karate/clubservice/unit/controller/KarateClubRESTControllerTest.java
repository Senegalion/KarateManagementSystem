package com.karate.clubservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.clubservice.api.controller.rest.KarateClubRESTController;
import com.karate.clubservice.api.exception.GlobalExceptionHandler;
import com.karate.clubservice.domain.exception.ClubNotFoundException;
import com.karate.clubservice.domain.exception.InvalidClubNameException;
import com.karate.clubservice.domain.model.dto.KarateClubDto;
import com.karate.clubservice.domain.service.KarateClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = KarateClubRESTController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class KarateClubRESTControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    KarateClubService service;

    @Test
    void getByName_returns200_andDto() throws Exception {
        // given
        String name = "GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE";
        KarateClubDto dto = KarateClubDto.builder()
                .karateClubId(11L)
                .name(name)
                .build();
        when(service.getByName(name)).thenReturn(dto);

        // when // then
        mvc.perform(get("/clubs/by-name").param("name", name))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.karateClubId").value(11))
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void getByName_returns400_whenInvalidName() throws Exception {
        // given
        String bad = "NOT_EXISTING";
        when(service.getByName(bad)).thenThrow(new InvalidClubNameException("Invalid club name: " + bad));

        // when // then
        mvc.perform(get("/clubs/by-name").param("name", bad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid club name")));
    }

    @Test
    void getByName_returns404_whenNotFound() throws Exception {
        // given
        String name = "KLUB_OKINAWA_KARATE_DO_WARSZAWA";
        when(service.getByName(name)).thenThrow(new ClubNotFoundException("Club not found: " + name));

        // when // then
        mvc.perform(get("/clubs/by-name").param("name", name))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Club not found")));
    }

    @Test
    void getById_returns200_andDto() throws Exception {
        // given
        long id = 7L;
        KarateClubDto dto = KarateClubDto.builder()
                .karateClubId(id)
                .name("WOLOMINSKI_KLUB_SHORIN_RYU_KARATE")
                .build();
        when(service.getById(id)).thenReturn(dto);

        // when // then
        mvc.perform(get("/clubs/by-id/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.karateClubId").value(7))
                .andExpect(jsonPath("$.name").value("WOLOMINSKI_KLUB_SHORIN_RYU_KARATE"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        // given
        long id = 404L;
        when(service.getById(id)).thenThrow(new ClubNotFoundException("Club not found with ID: " + id));

        // when // then
        mvc.perform(get("/clubs/by-id/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Club not found")));
    }
}
