package com.docpilot.backend.aiworker.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class IndexDocumentRequest(
    @JsonProperty("document_id")
    val documentId: UUID,
    val chunks: List<ChunkRequest>
)