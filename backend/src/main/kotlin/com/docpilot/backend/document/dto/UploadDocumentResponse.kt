package com.docpilot.backend.document.dto

import java.time.Instant
import java.util.UUID

data class UploadDocumentResponse(
    val id: UUID,
    val fileName: String,
    val size: Long,
    val uploadedAt: Instant,
    val summary: String? = null,
)