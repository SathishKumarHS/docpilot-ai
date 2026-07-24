package com.docpilot.backend.security

import com.docpilot.backend.auth.service.AnonymousSessionService
import tools.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AnonymousSessionFilter(
    private val sessionService: AnonymousSessionService,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    private val publicPaths = setOf(
        "/api/v1/health",
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/exchange",
        "/api/v1/auth/anonymous-session",
        "/error",
    )

    private val publicPrefixes = listOf("/actuator", "/oauth2", "/login/oauth2")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val path = request.servletPath
        if (path in publicPaths || publicPrefixes.any { path.startsWith(it) }) {
            chain.doFilter(request, response)
            return
        }

        val token = request.getHeader(ANON_TOKEN_HEADER)
        if (token != null) {
            val clientId = sessionService.verifyToken(token)
            if (clientId != null) {
                request.setAttribute(ANON_CLIENT_ID_ATTR, clientId)
                chain.doFilter(request, response)
                return
            }
        }

        val hasBearer = request.getHeader("Authorization")?.startsWith("Bearer ") == true
        if (hasBearer) {
            chain.doFilter(request, response)
            return
        }

        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(response.writer, mapOf(
            "timestamp" to java.time.Instant.now().toString(),
            "status" to 401,
            "error" to "Unauthorized",
            "message" to "Missing or invalid anonymous session",
        ))
    }

    companion object {
        const val ANON_TOKEN_HEADER = "X-Anonymous-Token"
        const val ANON_CLIENT_ID_ATTR = "anonymousClientId"
    }
}
