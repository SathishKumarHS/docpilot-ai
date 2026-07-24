package com.docpilot.backend.document.repository

import com.docpilot.backend.document.entity.DocumentChunkEntity
import jakarta.persistence.Id
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DocumentChunkRepository :
    JpaRepository<DocumentChunkEntity, UUID> {
    fun findByDocumentId(documentId: UUID): List<DocumentChunkEntity>

    @Transactional
    @Modifying
    @Query("delete from DocumentChunkEntity c where c.document.id = :documentId")
    fun deleteByDocumentId(
        @Param("documentId") documentId: UUID
    )
    }