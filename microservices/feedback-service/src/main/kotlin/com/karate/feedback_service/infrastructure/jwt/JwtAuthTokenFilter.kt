package com.karate.feedback_service.infrastructure.jwt;

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthTokenFilter(
        private val jwtConfigurationProperties: JwtConfigurationProperties
) : OncePerRequestFilter() {

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        val authorization = request.getHeader(AUTHORIZATION)
        if (authorization.isNullOrBlank() || !authorization.startsWith(BEARER)) {
            filterChain.doFilter(request, response)
            return
        }

        val authentication = getUsernamePasswordAuthenticationToken(authorization)
        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }

    private fun getUsernamePasswordAuthenticationToken(token: String): UsernamePasswordAuthenticationToken {
        val algorithm = Algorithm.HMAC256(jwtConfigurationProperties.secretKey)
        val verifier = JWT.require(algorithm).build()
        val decodedJWT = verifier.verify(token.substring(BEGIN_INDEX))

        val authorities = decodedJWT.getClaim("roles").asList(String::class.java)
                .map { SimpleGrantedAuthority(it) }

        return UsernamePasswordAuthenticationToken(decodedJWT.subject, null, authorities)
    }

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val BEARER = "Bearer "
        const val BEGIN_INDEX = 7
    }
}
