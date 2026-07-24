package com.docpilot.backend.auth.service

import com.docpilot.backend.auth.dto.AuthResponse
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${docpilot.jwt.refresh-token-expiration-days:30}") private val refreshTokenExpirationDays: Long,
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already registered")
        }

        val user = UserEntity(
            id = UUID.randomUUID(),
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            provider = Provider.LOCAL,
            role = Role.USER,
        )
        userRepository.save(user)

        return generateAuthResponse(user)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UnauthorizedException("Invalid email or password") }

        if (user.passwordHash == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        return generateAuthResponse(user)
    }

    @Transactional
    fun refreshToken(token: String): AuthResponse {
        val refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow { UnauthorizedException("Invalid or expired refresh token") }

        if (refreshToken.revoked || refreshToken.expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        refreshToken.revoked = true
        refreshTokenRepository.save(refreshToken)

        return generateAuthResponse(refreshToken.user)
    }

    fun generateAuthResponse(user: UserEntity): AuthResponse {
        val accessToken = jwtService.generateAccessToken(
            userId = user.id,
            email = user.email,
            role = user.role.name,
        )
        val refreshToken = createRefreshToken(user)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = user.id.toString(),
            email = user.email,
            role = user.role.name,
        )
    }

    fun createRefreshToken(user: UserEntity): String {
        val token = UUID.randomUUID().toString()
        val entity = RefreshTokenEntity(
            id = UUID.randomUUID(),
            user = user,
            token = token,
            expiresAt = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS),
        )
        refreshTokenRepository.save(entity)
        return token
    }
}
