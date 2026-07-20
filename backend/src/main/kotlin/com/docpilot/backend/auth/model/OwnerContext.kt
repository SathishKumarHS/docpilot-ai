package com.docpilot.backend.auth.model

import java.util.UUID


enum class OwnerType{
    USER,
    ANONYMOUS
}

data class OwnerContext(
    val ownerType: OwnerType,
    val ownerId: UUID
)