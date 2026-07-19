package com.docpilot.backend.document.service

import com.docpilot.backend.document.model.DocumentChunk
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DocumentChunkingService {

    fun chunk(text: String): List<DocumentChunk> {
        val chunkSize = 500

        return text
            .chunked(chunkSize).mapIndexed {
                index, content ->
                DocumentChunk(
                    id = UUID.randomUUID(),
                    chunkIndex = index,
                    content = content
                )
            }
    }
}