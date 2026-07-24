package com.docpilot.backend.oauth

import com.docpilot.backend.auth.dto.AuthResponse
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class AuthCodeStore {
    private val store = ConcurrentHashMap<String, Entry>()
    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "auth-code-cleanup").also { it.isDaemon = true }
    }

    @PostConstruct
    fun startCleanup() {
        scheduler.scheduleAtFixedRate({ store.entries.removeIf { it.value.isExpired() } }, 1, 1, TimeUnit.MINUTES)
    }

    fun create(response: AuthResponse): String {
        val code = UUID.randomUUID().toString()
        store[code] = Entry(response, Instant.now().plusSeconds(300))
        return code
    }

    fun consume(code: String): AuthResponse? {
        val entry = store.remove(code)
        return entry?.takeIf { !it.isExpired() }?.response
    }

    private class Entry(val response: AuthResponse, val expiresAt: Instant) {
        fun isExpired() = Instant.now().isAfter(expiresAt)
    }
}
