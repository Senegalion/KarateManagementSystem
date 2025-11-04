package com.karate.notification_service.infrastructure.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.mail")
public class EmailProperties {
    private String from;
    private String fromName;
}
