package com.docpilot.backend.ask.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class AskRequest(
    val question: String,
    @JsonProperty("document_id")
    val documentId: UUID? = null
)
