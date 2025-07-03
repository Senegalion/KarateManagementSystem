package com.karate.userservice;

import com.karate.userservice.infrastructure.security.jwt.JwtConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties(value = {JwtConfigurationProperties.class})
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
