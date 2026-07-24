package com.docpilot.backend.auth.repository

import com.docpilot.backend.auth.entity.Provider
import com.docpilot.backend.auth.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun findByProviderAndProviderUserId(provider: Provider, providerUserId: String): Optional<UserEntity>
    fun existsByEmail(email: String): Boolean
}
