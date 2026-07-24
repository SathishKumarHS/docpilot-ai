package com.docpilot.backend.oauth

import com.docpilot.backend.auth.dto.AuthResponse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthCodeStoreTest {
    private val store = AuthCodeStore()

    @Test
    fun `create and consume auth code`() {
        val authResponse = AuthResponse(
            userId = "user-id",
            email = "test@example.com",
            role = "USER",
            accessToken = "access",
            refreshToken = "refresh",
        )
        val code = store.create(authResponse)

        assertNotNull(code)

        val consumed = store.consume(code)
        assertNotNull(consumed)
        assertEquals("test@example.com", consumed.email)
    }

    @Test
    fun `consume returns null for invalid code`() {
        val result = store.consume("invalid-code")
        assertNull(result)
    }

    @Test
    fun `consume removes code after use`() {
        val authResponse = AuthResponse(
            userId = "user-id",
            email = "test@example.com",
            role = "USER",
            accessToken = "access",
            refreshToken = "refresh",
        )
        val code = store.create(authResponse)

        store.consume(code)
        val second = store.consume(code)
        assertNull(second)
    }
}
