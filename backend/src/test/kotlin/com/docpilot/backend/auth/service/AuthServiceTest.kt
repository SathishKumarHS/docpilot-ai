package com.docpilot.backend.auth.service

import com.docpilot.backend.anyNonNull
import com.docpilot.backend.auth.dto.LoginRequest
import com.docpilot.backend.auth.dto.RegisterRequest
import com.docpilot.backend.auth.entity.Provider
import com.docpilot.backend.auth.entity.RefreshTokenEntity
import com.docpilot.backend.auth.entity.Role
import com.docpilot.backend.auth.entity.UserEntity
import com.docpilot.backend.auth.jwt.JwtService
import com.docpilot.backend.auth.repository.RefreshTokenRepository
import com.docpilot.backend.auth.repository.UserRepository
import com.docpilot.backend.exception.ConflictException
import com.docpilot.backend.exception.UnauthorizedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.*
import kotlin.test.*

class AuthServiceTest {
    private val userRepository = mock(UserRepository::class.java)
    private val refreshTokenRepository = mock(RefreshTokenRepository::class.java)
    private val jwtService = mock(JwtService::class.java)
    private val passwordEncoder = mock(PasswordEncoder::class.java)
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        authService = AuthService(userRepository, refreshTokenRepository, jwtService, passwordEncoder, 30)
    }

    @Test
    fun `register creates user and returns auth response`() {
        val request = RegisterRequest("test@example.com", "password123")
        `when`(userRepository.existsByEmail("test@example.com")).thenReturn(false)
        `when`(passwordEncoder.encode("password123")).thenReturn("hashed")
        `when`(jwtService.generateAccessToken(anyNonNull(), anyNonNull(), anyNonNull())).thenReturn("access-token")
        `when`(userRepository.save(anyNonNull<UserEntity>())).thenAnswer { it.arguments[0] }

        val result = authService.register(request)

        assertNotNull(result)
        assertEquals("test@example.com", result.email)
        assertEquals("USER", result.role)
        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        verify(userRepository).save(anyNonNull<UserEntity>())
        verify(refreshTokenRepository).save(anyNonNull<RefreshTokenEntity>())
    }

    @Test
    fun `register throws when email exists`() {
        val request = RegisterRequest("existing@example.com", "password123")
        `when`(userRepository.existsByEmail("existing@example.com")).thenReturn(true)

        assertFailsWith<ConflictException> { authService.register(request) }
        verify(userRepository, never()).save(anyNonNull<UserEntity>())
    }

    @Test
    fun `login succeeds with valid credentials`() {
        val request = LoginRequest("test@example.com", "password123")
        val user = UserEntity(
            id = UUID.randomUUID(),
            email = "test@example.com",
            passwordHash = "hashed",
            provider = Provider.LOCAL,
            role = Role.USER,
        )
        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user))
        `when`(passwordEncoder.matches("password123", "hashed")).thenReturn(true)
        `when`(jwtService.generateAccessToken(anyNonNull(), anyNonNull(), anyNonNull())).thenReturn("access-token")

        val result = authService.login(request)

        assertNotNull(result)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun `login throws with wrong password`() {
        val request = LoginRequest("test@example.com", "wrong")
        val user = UserEntity(
            id = UUID.randomUUID(),
            email = "test@example.com",
            passwordHash = "hashed",
            provider = Provider.LOCAL,
            role = Role.USER,
        )
        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user))
        `when`(passwordEncoder.matches("wrong", "hashed")).thenReturn(false)

        assertFailsWith<UnauthorizedException> { authService.login(request) }
    }

    @Test
    fun `login throws when email not found`() {
        val request = LoginRequest("unknown@example.com", "password123")
        `when`(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty())

        assertFailsWith<UnauthorizedException> { authService.login(request) }
    }

    @Test
    fun `refresh succeeds with valid token`() {
        val user = UserEntity(
            id = UUID.randomUUID(),
            email = "test@example.com",
            passwordHash = "hashed",
            provider = Provider.LOCAL,
            role = Role.USER,
        )
        val refreshEntity = RefreshTokenEntity(
            id = UUID.randomUUID(),
            user = user,
            token = "valid-token",
            expiresAt = Instant.now().plusSeconds(3600),
        )
        `when`(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(refreshEntity))
        `when`(jwtService.generateAccessToken(anyNonNull(), anyNonNull(), anyNonNull())).thenReturn("new-access-token")

        val result = authService.refreshToken("valid-token")

        assertNotNull(result)
        assertTrue { refreshEntity.revoked }
        verify(refreshTokenRepository).save(refreshEntity)
    }

    @Test
    fun `refresh throws when token revoked`() {
        val user = UserEntity(
            id = UUID.randomUUID(),
            email = "test@example.com",
            passwordHash = "hashed",
            provider = Provider.LOCAL,
            role = Role.USER,
        )
        val refreshEntity = RefreshTokenEntity(
            id = UUID.randomUUID(),
            user = user,
            token = "revoked-token",
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = true,
        )
        `when`(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(refreshEntity))

        assertFailsWith<UnauthorizedException> { authService.refreshToken("revoked-token") }
    }

    @Test
    fun `refresh throws when token expired`() {
        val user = UserEntity(
            id = UUID.randomUUID(),
            email = "test@example.com",
            passwordHash = "hashed",
            provider = Provider.LOCAL,
            role = Role.USER,
        )
        val refreshEntity = RefreshTokenEntity(
            id = UUID.randomUUID(),
            user = user,
            token = "expired-token",
            expiresAt = Instant.now().minusSeconds(1),
        )
        `when`(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(refreshEntity))

        assertFailsWith<UnauthorizedException> { authService.refreshToken("expired-token") }
    }
}
