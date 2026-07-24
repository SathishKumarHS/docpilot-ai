package com.docpilot.backend.auth.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    val email: String,

    val passwordHash: String? = null,

    @Enumerated(EnumType.STRING)
    val provider: Provider = Provider.LOCAL,

    val providerUserId: String? = null,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER,

    val createdAt: Instant = Instant.now(),
)
