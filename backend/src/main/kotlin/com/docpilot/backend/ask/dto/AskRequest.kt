package com.docpilot.backend.ask.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class AskRequest(
    @field:NotBlank
    val question: String,
    @JsonProperty("document_id")
    val documentId: UUID? = null,
)
