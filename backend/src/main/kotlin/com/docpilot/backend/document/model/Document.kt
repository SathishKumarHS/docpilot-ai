package com.docpilot.backend.document.model

import java.time.Instant
import java.util.UUID

data class Document(
    val id: UUID,
    val fileName: String,
    val uploadedAt: Instant
)