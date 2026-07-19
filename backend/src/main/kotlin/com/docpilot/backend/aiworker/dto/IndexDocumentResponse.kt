package com.docpilot.backend.aiworker.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class IndexDocumentResponse(
    @JsonProperty(value = "indexed_chunks")
    val indexedChunks: Int
)