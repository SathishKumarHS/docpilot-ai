package com.docpilot.backend.document.service

import org.springframework.stereotype.Service

@Service
class DocumentChunkingService {

    fun chunk(text: String): List<String> {
        val chunkSize = 500

        return text
            .chunked(chunkSize)
    }
}