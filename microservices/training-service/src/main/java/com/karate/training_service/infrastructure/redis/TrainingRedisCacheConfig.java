package com.karate.training_service.infrastructure.redis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.karate.training_service.api.dto.TrainingSessionDto;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableCaching
@Configuration
public class TrainingRedisCacheConfig {

    @Bean
    @Primary
    public CacheManager trainingCacheManager(RedisConnectionFactory cf, ObjectMapper springMapper) {

        ObjectMapper om = springMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        var keySer = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());
        var genericSer = RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(om));

        var trainingSer = new Jackson2JsonRedisSerializer<>(TrainingSessionDto.class);
        trainingSer.setObjectMapper(om);

        var boolSer = new Jackson2JsonRedisSerializer<>(Boolean.class);
        boolSer.setObjectMapper(om);

        var longSer = new Jackson2JsonRedisSerializer<>(Long.class);
        longSer.setObjectMapper(om);

        var listType = om.getTypeFactory().constructCollectionType(List.class, TrainingSessionDto.class);
        var listSer = new Jackson2JsonRedisSerializer<>(listType);
        listSer.setObjectMapper(om);

        var def = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keySer)
                .serializeValuesWith(genericSer)
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .computePrefixWith(name -> "training-service:v1::" + name + "::");

        Map<String, RedisCacheConfiguration> per = new HashMap<>();

        per.put("trainingsByClub", def.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(listSer))
                .entryTtl(Duration.ofMinutes(10)));

        per.put("trainingById", def.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(trainingSer))
                .entryTtl(Duration.ofMinutes(15)));

        per.put("trainingExists", def.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(boolSer))
                .entryTtl(Duration.ofMinutes(5)));

        per.put("userClubIdByUsername_upstream", def.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(longSer))
                .entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(def)
                .withInitialCacheConfigurations(per)
                .build();
    }
}
