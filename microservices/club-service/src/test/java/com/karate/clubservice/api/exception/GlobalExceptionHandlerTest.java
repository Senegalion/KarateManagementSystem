package com.karate.clubservice.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestThrowingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, WebMvcAutoConfiguration.class})
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void validation_400_methodArgumentNotValid() throws Exception {
        mvc.perform(post("/__test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/__test/validation"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void malformedJson_400_httpMessageNotReadable() throws Exception {
        mvc.perform(post("/__test/malformed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-a-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"))
                .andExpect(jsonPath("$.path").value("/__test/malformed"));
    }

    @Test
    void invalidClubName_400() throws Exception {
        mvc.perform(get("/__test/invalid-club"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Invalid club name")))
                .andExpect(jsonPath("$.path").value("/__test/invalid-club"));
    }

    @Test
    void clubNotFound_404() throws Exception {
        mvc.perform(get("/__test/club-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Club not found")))
                .andExpect(jsonPath("$.path").value("/__test/club-not-found"));
    }

    @Test
    void noSuchElement_404() throws Exception {
        mvc.perform(get("/__test/no-such-element"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("nope")))
                .andExpect(jsonPath("$.path").value("/__test/no-such-element"));
    }

    @Test
    void entityNotFound_404() throws Exception {
        mvc.perform(get("/__test/entity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("missing")))
                .andExpect(jsonPath("$.path").value("/__test/entity-not-found"));
    }

    @Test
    void illegalState_409() throws Exception {
        mvc.perform(get("/__test/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("conflict"))
                .andExpect(jsonPath("$.path").value("/__test/illegal-state"));
    }

    @Test
    void dataIntegrity_409_fixedMessage() throws Exception {
        mvc.perform(get("/__test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username or email already exists"))
                .andExpect(jsonPath("$.path").value("/__test/data-integrity"));
    }

    @Test
    void generic_500() throws Exception {
        mvc.perform(get("/__test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message", containsString("Unexpected error: boom")))
                .andExpect(jsonPath("$.path").value("/__test/generic"));
    }
}
