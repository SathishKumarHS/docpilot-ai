package com.docpilot.backend.aiworker.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class ChunkRequest(
    @JsonProperty("chunk_id")
    val chunkId: UUID,
    @JsonProperty("chunk_index")
    val chunkIndex: Int,
    val text: String
)