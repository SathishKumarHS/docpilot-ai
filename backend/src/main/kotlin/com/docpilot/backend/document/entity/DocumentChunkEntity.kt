package com.docpilot.backend.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "document_chunks")
class DocumentChunkEntity (
    @Id
    val id: UUID,

    var chunkIndex: Int = 0,

    @Column(columnDefinition = "TEXT")
    var content: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    var document: DocumentEntity? = null
    )