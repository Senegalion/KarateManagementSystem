package com.karate.management.karatemanagementsystem;

import com.karate.management.karatemanagementsystem.infrastructure.security.jwt.JwtConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(value = {JwtConfigurationProperties.class})
@EnableScheduling
public class KarateManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(KarateManagementSystemApplication.class, args);
    }

}
