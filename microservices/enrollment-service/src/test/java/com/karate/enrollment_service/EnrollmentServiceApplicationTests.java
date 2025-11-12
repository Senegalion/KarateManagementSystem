package com.karate.enrollment_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.netflix.eureka.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.gateway.enabled=false",

        "spring.cloud.function.definition=",
        "spring.cloud.stream.bindings.input.destination=disabled",
        "spring.cloud.stream.bindings.output.destination=disabled",

        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.consumer.auto-startup=false"
})
class EnrollmentServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}