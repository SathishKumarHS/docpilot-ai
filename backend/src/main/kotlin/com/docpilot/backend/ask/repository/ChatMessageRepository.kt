package com.docpilot.backend.ask.repository

import com.docpilot.backend.ask.entity.ChatMessageEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessageEntity, UUID> {
    fun findByOwnerIdAndDocumentIdOrderByCreatedAtAsc(
        ownerId: UUID, documentId: UUID?, pageable: Pageable
    ): List<ChatMessageEntity>

    fun findByOwnerIdAndDocumentIdIsNullOrderByCreatedAtAsc(
        ownerId: UUID, pageable: Pageable
    ): List<ChatMessageEntity>
}
