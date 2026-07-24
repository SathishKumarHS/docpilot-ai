package com.docpilot.backend.security

import com.docpilot.backend.auth.service.AnonymousSessionService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class AnonymousSessionFilter(
    private val sessionService: AnonymousSessionService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        var clientId: UUID? = null
        val token = request.getHeader(ANON_TOKEN_HEADER)

        if (token != null) {
            clientId = sessionService.verifyToken(token)
        }

        if (clientId == null) {
            clientId = UUID.randomUUID()
            val newToken = sessionService.createToken(clientId)
            response.setHeader(ANON_TOKEN_HEADER, newToken)
        }

        request.setAttribute(ANON_CLIENT_ID_ATTR, clientId)
        chain.doFilter(request, response)
    }

    companion object {
        const val ANON_TOKEN_HEADER = "X-Anonymous-Token"
        const val ANON_CLIENT_ID_ATTR = "anonymousClientId"
    }
}
