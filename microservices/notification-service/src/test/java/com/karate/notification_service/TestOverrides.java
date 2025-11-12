package com.karate.notification_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@TestConfiguration
public class TestOverrides {

    @Bean
    static PropertySourcesPlaceholderConfigurer ignoreUnresolvedPlaceholders() {
        var cfg = new PropertySourcesPlaceholderConfigurer();
        cfg.setIgnoreUnresolvablePlaceholders(true);
        return cfg;
    }
}
