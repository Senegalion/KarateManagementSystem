package com.karate.feedback_service;

import com.karate.feedback_service.infrastructure.jwt.JwtConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(JwtConfigurationProperties::class)
class FeedbackServiceApplication
fun main(args: Array<String>) {
    runApplication<FeedbackServiceApplication>(*args)
}
