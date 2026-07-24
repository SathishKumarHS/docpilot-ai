package com.docpilot.backend.auth.service

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AnonymousSessionServiceTest {
    private val secret = Base64.getEncoder().encodeToString("test-secret-key-that-is-at-least-256-bits-long-for-hmac".toByteArray())
    private val service = AnonymousSessionService(secret, 24)

    @Test
    fun `create and verify token`() {
        val clientId = UUID.randomUUID()
        val token = service.createToken(clientId)

        val result = service.verifyToken(token)
        assertNotNull(result)
        assert(result == clientId)
    }

    @Test
    fun `verify invalid token returns null`() {
        val result = service.verifyToken("invalid-token")
        assertNull(result)
    }

    @Test
    fun `verify token with wrong key returns null`() {
        val otherSecret = Base64.getEncoder().encodeToString("other-secret-key-that-is-at-least-256-bits-long-for-hmac".toByteArray())
        val otherService = AnonymousSessionService(otherSecret, 24)
        val token = otherService.createToken(UUID.randomUUID())

        val result = service.verifyToken(token)
        assertNull(result)
    }
}
