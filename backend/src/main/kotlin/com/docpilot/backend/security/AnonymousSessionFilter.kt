package com.docpilot.backend.security

import com.docpilot.backend.auth.service.AnonymousSessionService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AnonymousSessionFilter(
    private val sessionService: AnonymousSessionService,
) : OncePerRequestFilter() {

    private val authPaths = setOf("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/anonymous-session")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
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
        if (hasBearer || request.servletPath in authPaths) {
            chain.doFilter(request, response)
            return
        }

        response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid anonymous session")
    }

    companion object {
        const val ANON_TOKEN_HEADER = "X-Anonymous-Token"
        const val ANON_CLIENT_ID_ATTR = "anonymousClientId"
    }
}
