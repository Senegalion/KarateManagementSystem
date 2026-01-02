package com.karate.authservice.api.exception;

import feign.FeignException;
import feign.Request;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadCredentials_returns401() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/auth/login");

        var resp = handler.handleBadCredentials(new BadCredentialsException("bad"), req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        com.karate.authservice.api.exception.dto.ErrorResponse body = resp.getBody();
        assertThat(body.status()).isEqualTo(401);
        assertThat(body.message()).isEqualTo("Invalid username or password");
        assertThat(body.path()).isEqualTo("/auth/login");
    }

    @Test
    void handleFeignConflict_returns409() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/auth/register");

        Request feignReq = Request.create(Request.HttpMethod.POST, "/user", Map.of(), null, StandardCharsets.UTF_8, null);
        FeignException.Conflict ex = new FeignException.Conflict("conflict", feignReq, null, Map.of());

        var resp = handler.handleFeignConflict(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().status()).isEqualTo(409);
        assertThat(resp.getBody().message()).isEqualTo("Username or email already exists");
    }

    @Test
    void handleFeignClientException_whenStatusNotResolvable_defaultsTo400() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/auth/register");

        Request feignReq = Request.create(Request.HttpMethod.GET, "/x", Map.of(), null, StandardCharsets.UTF_8, null);

        FeignException.FeignClientException ex = new FeignException.FeignClientException(
                999, "weird", feignReq, null, Map.of()
        ) {
        };

        var resp = handler.handleFeignClientException(ex, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().status()).isEqualTo(400);
        assertThat(resp.getBody().message()).contains("Upstream service error");
    }
}