package com.docpilot.backend.document.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentChunkingServiceTest {
    private val service = DocumentChunkingService()

    @Test
    fun `chunk splits text into 500-character chunks`() {
        val text = "a".repeat(1200)
        val chunks = service.chunk(text)

        assertEquals(3, chunks.size)
        assertEquals(500, chunks[0].content.length)
        assertEquals(500, chunks[1].content.length)
        assertEquals(200, chunks[2].content.length)
    }

    @Test
    fun `chunk assigns sequential indices`() {
        val text = "a".repeat(1000)
        val chunks = service.chunk(text)

        assertEquals(0, chunks[0].chunkIndex)
        assertEquals(1, chunks[1].chunkIndex)
    }

    @Test
    fun `chunk assigns unique ids`() {
        val text = "a".repeat(1000)
        val chunks = service.chunk(text)

        assertTrue(chunks[0].id != chunks[1].id)
    }

    @Test
    fun `chunk returns single chunk for short text`() {
        val text = "hello world"
        val chunks = service.chunk(text)

        assertEquals(1, chunks.size)
        assertEquals("hello world", chunks[0].content)
    }

    @Test
    fun `chunk returns empty list for empty text`() {
        val chunks = service.chunk("")

        assertTrue(chunks.isEmpty())
    }
}
