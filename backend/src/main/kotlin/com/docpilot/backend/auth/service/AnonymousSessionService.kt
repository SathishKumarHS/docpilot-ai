package com.docpilot.backend.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Service
class AnonymousSessionService(
    @Value("\${docpilot.jwt.secret}") secret: String,
    @Value("\${docpilot.anonymous-session.expiration-hours:24}") expirationHours: Long,
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))
    private val expirationMillis = TimeUnit.HOURS.toMillis(expirationHours)

    fun createToken(clientId: UUID): String {
        return Jwts.builder()
            .claim("clientId", clientId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMillis))
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
