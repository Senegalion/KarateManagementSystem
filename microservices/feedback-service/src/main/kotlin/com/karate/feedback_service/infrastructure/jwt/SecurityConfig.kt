package com.karate.feedback_service.infrastructure.jwt;

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Profile("!test")
@Configuration
class SecurityConfig(
    private val jwtAuthTokenFilter: JwtAuthTokenFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/", "/swagger-ui/**", "/swagger-ui.html/**",
                    "/v3/api-docs*/**", "/v2/api-docs*/**",
                    "/webjars/**", "/swagger-resources/**"
                ).permitAll()
                    .requestMatchers("/feedbacks/*").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/feedbacks/me").hasAnyRole("USER","ADMIN")
                    .requestMatchers("/feedbacks/admin/**").hasRole("ADMIN")
                    .requestMatchers("/feedbacks/*/*").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .httpBasic {}
            .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
