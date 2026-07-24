package com.docpilot.backend.auth.controller

import com.docpilot.backend.auth.dto.AuthResponse
import com.docpilot.backend.auth.dto.LoginRequest
import com.docpilot.backend.auth.dto.RegisterRequest
import com.docpilot.backend.auth.model.OwnerContext
import com.docpilot.backend.auth.model.OwnerType
import com.docpilot.backend.auth.service.AuthService
import com.docpilot.backend.document.service.DocumentService
import com.docpilot.backend.security.AnonymousSessionFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val documentService: DocumentService,
) {
    @PostMapping("/register")
    fun register(
        request: HttpServletRequest,
        @RequestBody @Valid registerRequest: RegisterRequest,
    ): AuthResponse {
        val response = authService.register(registerRequest)
        claimIfPresent(request, response)
        return response
    }

    @PostMapping("/login")
    fun login(
        request: HttpServletRequest,
        @RequestBody @Valid loginRequest: LoginRequest,
    ): AuthResponse {
        val response = authService.login(loginRequest)
        claimIfPresent(request, response)
        return response
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody body: Map<String, String>): AuthResponse {
        val token = body["refreshToken"] ?: throw IllegalArgumentException("refreshToken is required")
        return authService.refreshToken(token)
    }

    @PostMapping("/claim")
    fun claim(request: HttpServletRequest): Map<String, Any> {
        val userOwner = resolveOwner(request)
        val anonymousId = request.getAttribute(AnonymousSessionFilter.ANON_CLIENT_ID_ATTR) as? UUID
            ?: throw IllegalArgumentException("Missing anonymous session")
        val count = documentService.claimDocuments(anonymousId, userOwner)
        return mapOf("claimed" to count)
    }

    private fun claimIfPresent(request: HttpServletRequest, authResponse: AuthResponse) {
        val anonymousId = request.getAttribute(AnonymousSessionFilter.ANON_CLIENT_ID_ATTR) as? UUID
            ?: return
        val userOwner = OwnerContext(OwnerType.USER, UUID.fromString(authResponse.userId))
        documentService.claimDocuments(anonymousId, userOwner)
    }

    private fun resolveOwner(request: HttpServletRequest): OwnerContext {
        val userId = UUID.fromString(
            org.springframework.security.core.context.SecurityContextHolder
                .getContext().authentication.principal.toString()
        )
        return OwnerContext(OwnerType.USER, userId)
    }
}
