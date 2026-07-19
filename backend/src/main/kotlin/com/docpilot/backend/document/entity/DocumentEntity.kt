package com.docpilot.backend.document.entity

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
)