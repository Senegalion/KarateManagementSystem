package com.karate.userservice.infrastructure.redis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInfoDto;
import com.karate.userservice.api.dto.UserInformationDto;
import com.karate.userservice.api.dto.UserPayload;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
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
public class UserRedisCacheConfig {

    @Bean
    @Primary
    public CacheManager userCacheManager(RedisConnectionFactory cf, ObjectMapper springMapper) {
        ObjectMapper om = springMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        var keySer = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

        var genericSer = RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer(om));

        var userInfoSer = new Jackson2JsonRedisSerializer<>(UserInfoDto.class);
        userInfoSer.setObjectMapper(om);
        var userInformationSer = new Jackson2JsonRedisSerializer<>(UserInformationDto.class);
        userInformationSer.setObjectMapper(om);
        var userPayloadSer = new Jackson2JsonRedisSerializer<>(UserPayload.class);
        userPayloadSer.setObjectMapper(om);
        var boolSer = new Jackson2JsonRedisSerializer<>(Boolean.class);
        boolSer.setObjectMapper(om);
        var longSer = new Jackson2JsonRedisSerializer<>(Long.class);
        longSer.setObjectMapper(om);
        var authUserSer = new Jackson2JsonRedisSerializer<>(AuthUserDto.class);
        authUserSer.setObjectMapper(om);
        var clubSer = new Jackson2JsonRedisSerializer<>(KarateClubDto.class);
        clubSer.setObjectMapper(om);

        var listUserFromClubType = om.getTypeFactory().constructCollectionType(List.class, UserFromClubDto.class);
        var listUserFromClubSer = new Jackson2JsonRedisSerializer<>(listUserFromClubType); // konstruktor z JavaType
        listUserFromClubSer.setObjectMapper(om);

        var def = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keySer)
                .serializeValuesWith(genericSer)
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .computePrefixWith(name -> "user-service:v4::" + name + "::");

        Map<String, RedisCacheConfiguration> per = new HashMap<>();

        per.put("userInfoById", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(userInfoSer))
                .entryTtl(Duration.ofHours(1)));

        per.put("userPayloadById", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(userPayloadSer))
                .entryTtl(Duration.ofMinutes(15)));

        per.put("userExists", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(boolSer))
                .entryTtl(Duration.ofMinutes(5)));

        per.put("userClubIdByUsername", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(longSer))
                .entryTtl(Duration.ofMinutes(10)));

        per.put("usersByClubName", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(listUserFromClubSer))
                .entryTtl(Duration.ofMinutes(10)));

        per.put("currentUserInfo", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(userInformationSer))
                .entryTtl(Duration.ofMinutes(5)));

        per.put("authUserById", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(authUserSer))
                .entryTtl(Duration.ofMinutes(10)));

        per.put("authUserByUsername", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(authUserSer))
                .entryTtl(Duration.ofMinutes(10)));

        per.put("clubByName_upstream", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(clubSer))
                .entryTtl(Duration.ofMinutes(10)));

        per.put("clubById_upstream", def.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(clubSer))
                .entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(def)
                .withInitialCacheConfigurations(per)
                .build();
    }
}
