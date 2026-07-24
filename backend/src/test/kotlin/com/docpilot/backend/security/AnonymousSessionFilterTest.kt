package com.docpilot.backend.security

import com.docpilot.backend.auth.service.AnonymousSessionService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import tools.jackson.databind.ObjectMapper
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

class AnonymousSessionFilterTest {
    private val sessionService = Mockito.mock(AnonymousSessionService::class.java)
    private val objectMapper = ObjectMapper()
    private lateinit var filter: AnonymousSessionFilter

    @BeforeEach
    fun setup() {
        filter = AnonymousSessionFilter(sessionService, objectMapper)
    }

    @Test
    fun `passes through for public path`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        Mockito.`when`(request.servletPath).thenReturn("/api/v1/health")

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
    }

    @Test
    fun `passes through for actuator prefix`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        Mockito.`when`(request.servletPath).thenReturn("/actuator/info")

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
    }

    @Test
    fun `passes through with valid anonymous token`() {
        val clientId = UUID.randomUUID()
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        Mockito.`when`(request.servletPath).thenReturn("/api/v1/documents")
        Mockito.`when`(request.getHeader("X-Anonymous-Token")).thenReturn("valid-token")
        Mockito.`when`(sessionService.verifyToken("valid-token")).thenReturn(clientId)

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
        Mockito.verify(request).setAttribute("anonymousClientId", clientId)
    }

    @Test
    fun `passes through when Authorization Bearer is present`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        Mockito.`when`(request.servletPath).thenReturn("/api/v1/documents")
        Mockito.`when`(request.getHeader("Authorization")).thenReturn("Bearer some-token")

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
    }

    @Test
    fun `returns 401 when no token and no bearer`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)
        val writer = PrintWriter(StringWriter())

        Mockito.`when`(request.servletPath).thenReturn("/api/v1/documents")
        Mockito.`when`(request.getHeader("X-Anonymous-Token")).thenReturn(null)
        Mockito.`when`(request.getHeader("Authorization")).thenReturn(null)
        Mockito.`when`(response.writer).thenReturn(writer)

        filter.doFilter(request, response, chain)

        Mockito.verify(response).status = 401
    }
}
