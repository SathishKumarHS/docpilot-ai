package com.docpilot.backend.document.model

import java.util.UUID

data class DocumentChunk(
    val id: UUID,
    val chunkIndex: Int,
    val content: String,
)
