package com.karate.payment_service.infrastructure.messaging.config;

import com.karate.payment_service.infrastructure.messaging.dto.UserRegisteredEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {
    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> userRegisteredCF(KafkaProperties props) {
        Map<String, Object> cfg = props.buildConsumerProperties();
        cfg.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        cfg.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(
                cfg, new StringDeserializer(), new JsonDeserializer<>(UserRegisteredEvent.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> userRegisteredListenerFactory(
            ConsumerFactory<String, UserRegisteredEvent> cf) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent>();
        f.setConsumerFactory(cf);
        return f;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties props) {
        Map<String, Object> cfg = props.buildProducerProperties();
        cfg.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
        cfg.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(cfg);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}
