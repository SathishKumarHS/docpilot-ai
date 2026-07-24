package com.docpilot.backend.auth.controller

import com.docpilot.backend.auth.service.AnonymousSessionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AnonymousSessionController(
    private val sessionService: AnonymousSessionService,
) {
    @PostMapping("/anonymous-session")
    fun create(): Map<String, String> {
        val clientId = UUID.randomUUID()
        val token = sessionService.createToken(clientId)
        return mapOf("token" to token)
    }
}
