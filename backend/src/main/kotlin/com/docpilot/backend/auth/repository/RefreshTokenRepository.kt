package com.docpilot.backend.auth.repository

import com.docpilot.backend.auth.entity.RefreshTokenEntity
import com.docpilot.backend.auth.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByToken(token: String): Optional<RefreshTokenEntity>
    fun findByUserAndRevoked(user: UserEntity, revoked: Boolean): List<RefreshTokenEntity>
}
