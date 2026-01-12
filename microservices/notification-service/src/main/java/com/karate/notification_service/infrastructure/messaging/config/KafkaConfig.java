package com.karate.notification_service.infrastructure.messaging.config;

import com.karate.notification_service.infrastructure.messaging.dto.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConfig {

    private static <T> ConsumerFactory<String, T> factoryFor(KafkaProperties props, Class<T> type) {
        Map<String, Object> cfg = props.buildConsumerProperties();
        cfg.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        cfg.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(cfg, new StringDeserializer(), new JsonDeserializer<>(type, false));
    }

    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> userRegisteredConsumerFactory(KafkaProperties props) {
        return factoryFor(props, UserRegisteredEvent.class);
    }

    @Bean
    public ConsumerFactory<String, EnrollmentEvent> enrollmentConsumerFactory(KafkaProperties props) {
        return factoryFor(props, EnrollmentEvent.class);
    }

    @Bean
    public ConsumerFactory<String, FeedbackEvent> feedbackConsumerFactory(KafkaProperties props) {
        return factoryFor(props, FeedbackEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> userRegisteredListenerFactory(
            ConsumerFactory<String, UserRegisteredEvent> cf) {
        ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(cf);
        return f;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EnrollmentEvent> enrollmentListenerFactory(
            ConsumerFactory<String, EnrollmentEvent> cf) {
        ConcurrentKafkaListenerContainerFactory<String, EnrollmentEvent> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(cf);
        return f;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FeedbackEvent> feedbackListenerFactory(
            ConsumerFactory<String, FeedbackEvent> cf) {
        ConcurrentKafkaListenerContainerFactory<String, FeedbackEvent> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(cf);
        return f;
    }

    @Bean
    public ConsumerFactory<String, TrainingCreatedEvent> trainingCreatedConsumerFactory(KafkaProperties props) {
        return factoryFor(props, TrainingCreatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TrainingCreatedEvent> trainingCreatedListenerFactory(
            ConsumerFactory<String, TrainingCreatedEvent> cf) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, TrainingCreatedEvent>();
        f.setConsumerFactory(cf);
        return f;
    }

    @Bean
    public ConsumerFactory<String, TrainingDeletedEvent> trainingDeletedConsumerFactory(KafkaProperties props) {
        return factoryFor(props, TrainingDeletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TrainingDeletedEvent> trainingDeletedListenerFactory(
            ConsumerFactory<String, TrainingDeletedEvent> cf) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, TrainingDeletedEvent>();
        f.setConsumerFactory(cf);
        return f;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserDeletedEvent> userDeletedListenerFactory(
            ConsumerFactory<String, UserDeletedEvent> cf) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, UserDeletedEvent>();
        f.setConsumerFactory(cf);
        return f;
    }

    @Bean
    public ConsumerFactory<String, UserDeletedEvent> userDeletedConsumerFactory(KafkaProperties props) {
        return factoryFor(props, UserDeletedEvent.class);
    }
}
