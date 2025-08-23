package com.karate.feedback_service.infrastructure.jwt;

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
        private val jwtAuthTokenFilter: JwtAuthTokenFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
            http
                    .cors { it.configurationSource(corsConfigurationSource()) }
                    .csrf { it.disable() }
                    .authorizeHttpRequests {
                        it.requestMatchers(
                                "/", "/swagger-ui/**", "/swagger-ui.html/**",
                                "/v3/api-docs*/**", "/v2/api-docs*/**",
                                "/webjars/**", "/swagger-resources/**"
                        ).permitAll()
                                .requestMatchers("/users/by-club").hasRole(ADMIN)
                                .requestMatchers("/users/me").hasAnyRole("USER", ADMIN)
                                .requestMatchers("/internal/users/**").permitAll()
                                .requestMatchers("/users/**").hasAnyRole("USER", ADMIN)
                                .anyRequest().authenticated()
                    }
                    .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                    .httpBasic {}
                    .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
                    .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000", "http://localhost:5173")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

    companion object {
        const val ADMIN = "ADMIN"
    }
}
