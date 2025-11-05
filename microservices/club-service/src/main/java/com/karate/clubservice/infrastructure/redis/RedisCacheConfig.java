package com.karate.clubservice.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.clubservice.domain.model.dto.KarateClubDto;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf, ObjectMapper baseMapper) {
        var keySer = RedisSerializationContext
                .SerializationPair.fromSerializer(new StringRedisSerializer());

        var clubDtoSer = new Jackson2JsonRedisSerializer<>(KarateClubDto.class);
        clubDtoSer.setObjectMapper(baseMapper.copy());

        var valueSer = RedisSerializationContext
                .SerializationPair.fromSerializer(clubDtoSer);

        // default (gdybyś dodał inne cache — mogą używać innego TTL)
        RedisCacheConfiguration defaultCfg = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(keySer)
                .serializeValuesWith(valueSer)
                .entryTtl(Duration.ofHours(12))
                .disableCachingNullValues()
                .computePrefixWith(name -> "club-service::" + name + "::");

        // per-cache (możesz różnicować TTL)
        Map<String, RedisCacheConfiguration> perCache = Map.of(
                "clubById", defaultCfg.entryTtl(Duration.ofHours(24)),
                "clubByName", defaultCfg.entryTtl(Duration.ofHours(24))
        );

        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaultCfg)
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}
