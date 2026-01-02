package com.karate.training_service.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class UpstreamGatewayTest {

    @Test
    void fallback_getUserClubId_throwsUpstreamUnavailable() {
        var client = mock(com.karate.training_service.infrastructure.client.UserServiceClient.class);
        var gw = new com.karate.training_service.domain.service.UpstreamGateway(client);

        var ex = new RuntimeException("down");
        assertThatThrownBy(() -> gw.getUserClubIdFallback("john", ex))
                .isInstanceOf(com.karate.training_service.domain.exception.UpstreamUnavailableException.class)
                .hasMessageContaining("user-service unavailable")
                .hasCause(ex);
    }
}