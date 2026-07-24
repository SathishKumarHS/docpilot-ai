package com.docpilot.backend.oauth

import com.docpilot.backend.anyNonNull
import com.docpilot.backend.auth.dto.AuthResponse
import com.docpilot.backend.auth.entity.Provider
import com.docpilot.backend.auth.entity.Role
import com.docpilot.backend.auth.entity.UserEntity
import com.docpilot.backend.auth.jwt.JwtService
import com.docpilot.backend.auth.repository.UserRepository
import com.docpilot.backend.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.*

class OAuth2SuccessHandlerTest {
    private val userRepository = mock(UserRepository::class.java)
    private val jwtService = mock(JwtService::class.java)
    private val authService = mock(AuthService::class.java)
    private val authCodeStore = mock(AuthCodeStore::class.java)

    @Test
    fun `onAuthenticationSuccess creates new user and redirects`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val authToken = mock(OAuth2AuthenticationToken::class.java)
        val oauth2User = mock(OAuth2User::class.java)

        `when`(authToken.principal).thenReturn(oauth2User)
        `when`(oauth2User.attributes).thenReturn(
            mapOf("email" to "new@example.com", "sub" to "google-id-1")
        )
        `when`(userRepository.findByProviderAndProviderUserId(Provider.GOOGLE, "google-id-1"))
            .thenReturn(Optional.empty())
        `when`(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty())
        `when`(userRepository.save(anyNonNull<UserEntity>())).thenAnswer { it.arguments[0] as UserEntity }
        `when`(authService.generateAuthResponse(anyNonNull<UserEntity>())).thenReturn(
            AuthResponse("uid", "new@example.com", "USER", "access", "refresh")
        )
        `when`(authCodeStore.create(anyNonNull())).thenReturn("test-code")

        val handler = OAuth2SuccessHandler(userRepository, jwtService, authService, authCodeStore, "http://localhost:5173")

        handler.onAuthenticationSuccess(request, response, authToken)

        verify(response).sendRedirect("http://localhost:5173/auth/callback?code=test-code")
    }

    @Test
    fun `onAuthenticationSuccess uses existing user`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val authToken = mock(OAuth2AuthenticationToken::class.java)
        val oauth2User = mock(OAuth2User::class.java)
        val existingUser = UserEntity(
            id = UUID.randomUUID(),
            email = "existing@example.com",
            provider = Provider.GOOGLE,
            providerUserId = "google-id-1",
            role = Role.USER,
        )
        `when`(authToken.principal).thenReturn(oauth2User)
        `when`(oauth2User.attributes).thenReturn(
            mapOf("email" to "existing@example.com", "sub" to "google-id-1")
        )
        `when`(userRepository.findByProviderAndProviderUserId(Provider.GOOGLE, "google-id-1"))
            .thenReturn(Optional.of(existingUser))
        `when`(authService.generateAuthResponse(existingUser)).thenReturn(
            AuthResponse("uid", "existing@example.com", "USER", "access", "refresh")
        )
        `when`(authCodeStore.create(anyNonNull())).thenReturn("test-code")

        val handler = OAuth2SuccessHandler(userRepository, jwtService, authService, authCodeStore, "http://localhost:5173")

        handler.onAuthenticationSuccess(request, response, authToken)

        verify(response).sendRedirect("http://localhost:5173/auth/callback?code=test-code")
        verify(userRepository, never()).save(anyNonNull())
    }
}
