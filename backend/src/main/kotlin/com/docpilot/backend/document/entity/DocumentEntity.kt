package com.docpilot.backend.document.entity

import com.docpilot.backend.auth.model.OwnerType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "documents")
class DocumentEntity(
    @Id
    val id: UUID,

    val fileName: String,

    val size: Long,

    val uploadedAt: Instant,

    var ownerId : UUID,

    var ownerType : OwnerType,

    var storageKey: String? = null,

    @Column(columnDefinition = "TEXT")
    var summary: String? = null,
)