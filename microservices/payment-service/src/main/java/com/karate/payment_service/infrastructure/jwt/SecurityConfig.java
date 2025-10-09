package com.karate.payment_service.infrastructure.jwt;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@AllArgsConstructor
public class SecurityConfig {
    public static final String ADMIN = "ADMIN";
    private final JwtAuthTokenFilter jwtAuthTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html/**").permitAll()
                        .requestMatchers("/v3/api-docs*/**").permitAll()
                        .requestMatchers("/v2/api-docs*/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/payments/me/unpaid").hasAnyRole("USER", ADMIN)
                        .requestMatchers("/payments/me/history").hasAnyRole("USER", ADMIN)
                        .requestMatchers("/payments/me/create-order").hasAnyRole("USER", ADMIN)
                        .requestMatchers("/payments/capture/{orderId}").hasAnyRole("USER", ADMIN)
                        .requestMatchers("/payments/admin/payments/manual").hasRole(ADMIN)
                        .requestMatchers("/payments/admin/payments/user/{userId}/unpaid").hasRole(ADMIN)
                        .requestMatchers("/payments/admin/payments/user/{userId}/history").hasRole(ADMIN)
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
