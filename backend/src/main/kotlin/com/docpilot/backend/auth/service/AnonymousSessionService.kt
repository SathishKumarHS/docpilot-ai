package com.docpilot.backend.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class AnonymousSessionService(
    @Value("\${docpilot.jwt.secret}") secret: String,
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun createToken(clientId: UUID): String {
        return Jwts.builder()
            .claim("clientId", clientId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + 86400000L))
            .signWith(signingKey)
            .compact()
    }

    fun verifyToken(token: String): UUID? = try {
        val claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
        UUID.fromString(claims.get("clientId", String::class.java))
    } catch (_: Exception) {
        null
    }
}
