package com.docpilot.backend.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${docpilot.jwt.secret}") secret: String,
    @Value("\${docpilot.jwt.access-token-expiration:900000}") private val accessTokenExpiration: Long,
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(userId: UUID, email: String, role: String): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + accessTokenExpiration))
            .signWith(signingKey)
            .compact()
    }

    fun validateToken(token: String): Claims? = try {
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    } catch (e: Exception) {
        null
    }
}
