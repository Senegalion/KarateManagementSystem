package com.karate.payment_service.api.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.karate.payment_service.api.dto.*;
import com.karate.payment_service.domain.service.AuthResolver;
import com.karate.payment_service.domain.service.PaymentApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentRESTController.class)
class PaymentRESTControllerWebMvcTest {

    @MockBean PaymentApplicationService service;
    @MockBean AuthResolver authResolver;

    @org.springframework.beans.factory.annotation.Autowired MockMvc mvc;

    ObjectMapper om;

    @BeforeEach
    void setup() {
        om = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void unauthorized_whenNoUser() throws Exception {
        mvc.perform(get("/payments/me/unpaid"))
                .andExpect(status().isUnauthorized()); // bo SecurityConfig
    }

    @Test
    @WithMockUser(roles = "USER")
    void myUnpaid_delegatesWithResolvedUserId() throws Exception {
        when(authResolver.resolveUserId(any(Authentication.class))).thenReturn(7L);
        when(service.getUnpaidSummary(7L)).thenReturn(new UnpaidSummaryDto(List.of("2025-01"), new BigDecimal("60.00"), new BigDecimal("60.00")));

        mvc.perform(get("/payments/me/unpaid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months[0]").value("2025-01"));

        verify(service).getUnpaidSummary(7L);
    }

//    @Test
//    @WithMockUser(roles = "USER")
//    void createOrder_me_buildsDelegateRequest() throws Exception {
//        when(authResolver.resolveUserId(any(Authentication.class))).thenReturn(7L);
//        when(service.createOrder(any())).thenReturn(new CreateOrderResponse("OID", "url", new BigDecimal("60.00"), "PLN", List.of("2025-01"), com.karate.payment_service.domain.model.PaymentStatus.PENDING));
//
//        var req = new PaymentRESTController.CreateOrderMeRequest(
//                List.of(YearMonth.of(2025, 1)), "PLN", "http://ret", "http://can"
//        );
//
//        mvc.perform(post("/payments/me/create-order")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(om.writeValueAsString(req)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.providerOrderId").value("OID"));
//
//        ArgumentCaptor<CreateOrderRequest> cap = ArgumentCaptor.forClass(CreateOrderRequest.class);
//        verify(service).createOrder(cap.capture());
//        assertThat(cap.getValue().userId()).isEqualTo(7L);
//        assertThat(cap.getValue().months()).containsExactly(YearMonth.of(2025,1));
//        assertThat(cap.getValue().returnUrl()).isEqualTo("http://ret");
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    void capture_delegates() throws Exception {
//        when(service.capture("OID")).thenReturn(new CaptureResponse("OID", "PAID"));
//
//        mvc.perform(post("/payments/capture/OID"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("PAID"));
//
//        verify(service).capture("OID");
//    }

    @Test
    @WithMockUser(roles = "USER")
    void admin_manual_forbidden_forUser() throws Exception {
        mvc.perform(post("/payments/admin/payments/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"months\":[\"2025-01\"]}"))
                .andExpect(status().isForbidden());
    }

//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void admin_manual_ok_forAdmin() throws Exception {
//        mvc.perform(post("/payments/admin/payments/manual")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"userId\":1,\"months\":[\"2025-01\",\"2025-02\"]}"))
//                .andExpect(status().isOk());
//
//        verify(service).manualPayment(eq(1L), any());
//    }
}
