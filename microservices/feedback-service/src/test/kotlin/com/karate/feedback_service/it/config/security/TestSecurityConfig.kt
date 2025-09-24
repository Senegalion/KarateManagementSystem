package com.karate.feedback_service.it.config.security;

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.filter.OncePerRequestFilter

@TestConfiguration
@Profile("test")
class TestSecurityConfig {

    /**
     * Headers:
     *  - X-Test-User: username
     *  - X-Test-Roles: CSV without 'ROLE_' (e.g. ADMIN,USER)
     */
    @Bean
    @Primary
    fun testJwtAuthTokenFilter() = object : OncePerRequestFilter() {
        override fun doFilterInternal(
            req: HttpServletRequest,
            res: HttpServletResponse,
            chain: FilterChain
        ) {
            val user = req.getHeader("X-Test-User")
            val roles = req.getHeader("X-Test-Roles")
            if (user != null) {
                val authorities = roles
                    ?.split(",")
                    ?.mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
                    ?.map { SimpleGrantedAuthority("ROLE_$it") }
                    ?: emptyList()
                val auth = UsernamePasswordAuthenticationToken(user, "N/A", authorities)
                SecurityContextHolder.getContext().authentication = auth
            }
            chain.doFilter(req, res)
        }
    }

    @Bean
    @Primary
    fun securityFilterChain(http: HttpSecurity, testJwtAuthTokenFilter: OncePerRequestFilter): SecurityFilterChain =
        http
            .csrf(AbstractHttpConfigurer<*, *>::disable)
            .anonymous(AbstractHttpConfigurer<*, *>::disable)
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers(HttpMethod.POST, "/feedbacks/**").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.GET, "/feedbacks/**").hasAnyRole("USER", "ADMIN")
                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, res, _ -> res.sendError(401) }
                it.accessDeniedHandler { _, res, _ -> res.sendError(403) }
            }
            .addFilterBefore(
                testJwtAuthTokenFilter,
                org.springframework.security.web.access.intercept.AuthorizationFilter::class.java
            )
            .build()
}
