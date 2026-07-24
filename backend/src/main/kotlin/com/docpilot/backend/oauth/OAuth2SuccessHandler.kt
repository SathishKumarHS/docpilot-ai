package com.docpilot.backend.oauth

import com.docpilot.backend.auth.entity.Provider
import com.docpilot.backend.auth.entity.Role
import com.docpilot.backend.auth.entity.UserEntity
import com.docpilot.backend.auth.jwt.JwtService
import com.docpilot.backend.auth.repository.UserRepository
import com.docpilot.backend.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OAuth2SuccessHandler(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val authService: AuthService,
    private val authCodeStore: AuthCodeStore,
    @Value("\${docpilot.frontend-url}") private val frontendUrl: String,
) : AuthenticationSuccessHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val token = authentication as OAuth2AuthenticationToken
        val attributes = token.principal.attributes

        val email = attributes["email"] as? String
            ?: throw IllegalArgumentException("Email not provided by OAuth provider")

        val providerUserId = attributes["sub"] as? String
            ?: throw IllegalArgumentException("User ID not provided by OAuth provider")

        val user = userRepository.findByProviderAndProviderUserId(Provider.GOOGLE, providerUserId)
            .orElseGet {
                userRepository.findByEmail(email).orElseGet {
                    userRepository.save(
                        UserEntity(
                            id = UUID.randomUUID(),
                            email = email,
                            provider = Provider.GOOGLE,
                            providerUserId = providerUserId,
                            role = Role.USER,
                        )
                    )
                }.also {
                    log.info("Existing user logged in via Google: {}", email)
                }
            }

        val authResponse = authService.generateAuthResponse(user)
        val code = authCodeStore.create(authResponse)

        response.sendRedirect("$frontendUrl/auth/callback?code=$code")
    }
}
