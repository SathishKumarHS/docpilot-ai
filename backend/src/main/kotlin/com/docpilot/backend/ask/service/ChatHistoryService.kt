package com.docpilot.backend.ask.service

import com.docpilot.backend.ask.entity.ChatMessageEntity
import com.docpilot.backend.ask.model.MessageRole
import com.docpilot.backend.ask.repository.ChatMessageRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class ChatHistoryService(
    private val repository: ChatMessageRepository,
) {

    fun getHistory(ownerId: UUID, documentId: UUID? = null, pageable: Pageable = Pageable.unpaged()): List<ChatMessageEntity> {
        return if (documentId != null) {
            repository.findByOwnerIdAndDocumentIdOrderByCreatedAtAsc(ownerId, documentId, pageable)
        } else {
            repository.findByOwnerIdAndDocumentIdIsNullOrderByCreatedAtAsc(ownerId, pageable)
        }
    }

    fun addMessage(ownerId: UUID, documentId: UUID?, role: MessageRole, content: String) {
        repository.save(
            ChatMessageEntity(
                id = UUID.randomUUID(),
                ownerId = ownerId,
                documentId = documentId,
                role = role,
                content = content,
                createdAt = Instant.now(),
            )
        )
    }
}
