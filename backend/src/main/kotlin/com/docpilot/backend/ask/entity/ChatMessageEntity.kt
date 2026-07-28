package com.docpilot.backend.ask.entity

import com.docpilot.backend.ask.model.MessageRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "chat_messages",
    indexes = [
        Index(columnList = "ownerId, documentId, createdAt"),
    ],
)
class ChatMessageEntity(
    @Id
    val id: UUID,

    val ownerId: UUID,

    val documentId: UUID? = null,

    @Enumerated(EnumType.STRING)
    val role: MessageRole,

    @Column(columnDefinition = "TEXT")
    val content: String,

    val createdAt: Instant,
)
