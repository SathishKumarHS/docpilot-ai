package com.docpilot.backend.document.repository

import com.docpilot.backend.auth.model.OwnerType
import com.docpilot.backend.document.entity.DocumentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface DocumentRepository : JpaRepository<DocumentEntity, UUID> {
    fun findByOwnerTypeAndOwnerId(ownerType: OwnerType, ownerId: UUID): DocumentEntity?
    fun countByOwnerTypeAndOwnerId(ownerType: OwnerType, ownerId: UUID): Long
    fun findByIdAndOwnerTypeAndOwnerId(id: UUID, ownerType: OwnerType, ownerId: UUID): DocumentEntity?
    fun findByOwnerTypeAndOwnerId(ownerType: OwnerType, ownerId: UUID, pageable: Pageable): Page<DocumentEntity>
    fun findAllByOwnerTypeAndOwnerId(ownerType: OwnerType, ownerId: UUID): List<DocumentEntity>
    fun findAllByUploadedAtBefore(uploadedAt: Instant): List<DocumentEntity>
}
