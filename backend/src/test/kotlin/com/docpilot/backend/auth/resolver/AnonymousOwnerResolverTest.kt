package com.docpilot.backend.auth.resolver

import com.docpilot.backend.auth.model.OwnerType
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnonymousOwnerResolverTest {
    private val resolver = AnonymousOwnerResolver()

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `resolve returns USER when authenticated`() {
        val userId = UUID.randomUUID()
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId.toString(), null, listOf())

        val result = resolver.resolve(Mockito.mock(HttpServletRequest::class.java))

        assertEquals(OwnerType.USER, result.ownerType)
        assertEquals(userId, result.ownerId)
    }

    @Test
    fun `resolve returns ANONYMOUS when clientId in attribute`() {
        SecurityContextHolder.getContext().authentication = null
        val clientId = UUID.randomUUID()
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.getAttribute("anonymousClientId")).thenReturn(clientId)

        val result = resolver.resolve(request)

        assertEquals(OwnerType.ANONYMOUS, result.ownerType)
        assertEquals(clientId, result.ownerId)
    }

    @Test
    fun `resolve throws when no auth and no session`() {
        SecurityContextHolder.getContext().authentication = null
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.getAttribute("anonymousClientId")).thenReturn(null)

        assertFailsWith<IllegalArgumentException> {
            resolver.resolve(request)
        }
    }
}
