package com.karate.authservice.infrastructure.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorrelationIdFilterTest {

    @Test
    void correlationIdFilter_setsHeader_andMdc() throws Exception {
        var f = new com.karate.authservice.infrastructure.logging.CorrelationIdFilter();

        var req = new org.springframework.mock.web.MockHttpServletRequest();
        req.addHeader("X-Correlation-Id", "cid-123");
        var res = new org.springframework.mock.web.MockHttpServletResponse();

        var chain = mock(jakarta.servlet.FilterChain.class);

        f.doFilter(req, res, chain);

        assertThat(res.getHeader("X-Correlation-Id")).isEqualTo("cid-123");
        verify(chain).doFilter(any(), any());
    }

    @Test
    void correlationIdFilter_generatesWhenMissing() throws Exception {
        var f = new com.karate.authservice.infrastructure.logging.CorrelationIdFilter();

        var req = new org.springframework.mock.web.MockHttpServletRequest();
        var res = new org.springframework.mock.web.MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        f.doFilter(req, res, chain);

        assertThat(res.getHeader("X-Correlation-Id")).isNotBlank();
    }
}