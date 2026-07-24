package com.docpilot.backend.security

import com.docpilot.backend.auth.jwt.JwtService
import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.security.core.context.SecurityContextHolder

class JwtFilterTest {
    private val jwtService = Mockito.mock(JwtService::class.java)
    private val filter = JwtFilter(jwtService)

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `passes through when no auth header`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        Mockito.`when`(request.getHeader("Authorization")).thenReturn(null)

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
        Mockito.verifyNoMoreInteractions(jwtService)
    }

    @Test
    fun `passes through when auth header does not start with Bearer`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        Mockito.`when`(request.getHeader("Authorization")).thenReturn("Basic token")

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
        Mockito.verifyNoMoreInteractions(jwtService)
    }

    @Test
    fun `passes through when token is invalid`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        Mockito.`when`(request.getHeader("Authorization")).thenReturn("Bearer invalid-token")
        Mockito.`when`(jwtService.validateToken("invalid-token")).thenReturn(null)

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
    }

    @Test
    fun `sets authentication when token is valid`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        val claims = Mockito.mock(Claims::class.java)
        Mockito.`when`(request.getHeader("Authorization")).thenReturn("Bearer valid-token")
        Mockito.`when`(claims.subject).thenReturn("user-id")
        Mockito.`when`(claims.get("role", String::class.java)).thenReturn("USER")
        Mockito.`when`(jwtService.validateToken("valid-token")).thenReturn(claims)

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
        val auth = SecurityContextHolder.getContext().authentication
        assert(auth != null)
        assert(auth!!.principal == "user-id")
        assert(auth.authorities.any { it.authority == "ROLE_USER" })
    }
}
