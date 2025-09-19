package com.karate.training_service.infrastructure.client.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class FeignClientConfig {
    @Bean
    public RequestInterceptor correlationIdRequestInterceptor() {
        return template -> {
            String cid = Optional.ofNullable(MDC.get("correlationId"))
                    .orElse(UUID.randomUUID().toString());
            template.header("X-Correlation-Id", cid);

            String traceId = MDC.get("traceId");
            if (traceId != null) template.header("X-Trace-Id", traceId);
        };
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // DEV: FULL, PROD: BASIC
    }
}
