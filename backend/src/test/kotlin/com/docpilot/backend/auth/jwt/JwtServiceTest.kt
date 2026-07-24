package com.docpilot.backend.auth.jwt

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JwtServiceTest {
    private val secret = Base64.getEncoder().encodeToString("test-secret-key-that-is-at-least-256-bits-long-for-hmac".toByteArray())
    private val jwtService = JwtService(secret, 900000)

    @Test
    fun `generate and validate token`() {
        val userId = UUID.randomUUID()
        val token = jwtService.generateAccessToken(userId, "test@example.com", "USER")

        val claims = jwtService.validateToken(token)
        assertNotNull(claims)
        assert(claims.subject == userId.toString())
        assert(claims.get("email", String::class.java) == "test@example.com")
        assert(claims.get("role", String::class.java) == "USER")
    }

    @Test
    fun `validate invalid token returns null`() {
        val result = jwtService.validateToken("invalid-token")
        assertNull(result)
    }

    @Test
    fun `validate expired token returns null`() {
        val expiredService = JwtService(secret, -1)
        val token = expiredService.generateAccessToken(UUID.randomUUID(), "test@example.com", "USER")

        val result = jwtService.validateToken(token)
        assertNull(result)
    }
}
