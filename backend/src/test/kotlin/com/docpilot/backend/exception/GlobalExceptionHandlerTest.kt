package com.docpilot.backend.exception

import com.docpilot.backend.aiworker.exception.AiWorkerException
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleUnsupportedDocumentType returns 415`() {
        val ex = UnsupportedDocumentTypeException("Only PDF files are supported")
        val response = handler.handleUnsupportedDocumentType(ex)

        assert(response.statusCode == HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        assert(response.body?.status == 415)
        assert(response.body?.message == "Only PDF files are supported")
    }

    @Test
    fun `handleUnsupportedDocumentSize returns 413`() {
        val ex = UnsupportedDocumentSizeException("File too large")
        val response = handler.handleUnsupportedDocumentSize(ex)

        assert(response.statusCode == HttpStatus.PAYLOAD_TOO_LARGE)
        assert(response.body?.status == 413)
    }

    @Test
    fun `handleAiWorkerError returns 502`() {
        val ex = AiWorkerException("AI service error")
        val response = handler.handleAiWorkerError(ex)

        assert(response.statusCode == HttpStatus.BAD_GATEWAY)
        assert(response.body?.status == 502)
    }

    @Test
    fun `handleUnauthorized returns 401`() {
        val ex = UnauthorizedException("Invalid credentials")
        val response = handler.handleUnauthorized(ex)

        assert(response.statusCode == HttpStatus.UNAUTHORIZED)
        assert(response.body?.status == 401)
    }

    @Test
    fun `handleNotFound returns 404`() {
        val ex = NotFoundException("Document not found")
        val response = handler.handleNotFound(ex)

        assert(response.statusCode == HttpStatus.NOT_FOUND)
        assert(response.body?.status == 404)
    }

    @Test
    fun `handleConflict returns 409`() {
        val ex = ConflictException("Email already exists")
        val response = handler.handleConflict(ex)

        assert(response.statusCode == HttpStatus.CONFLICT)
        assert(response.body?.status == 409)
    }

    @Test
    fun `handleBadRequest returns 400`() {
        val ex = IllegalArgumentException("Invalid input")
        val response = handler.handleBadRequest(ex)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.status == 400)
    }

    @Test
    fun `handleGeneric returns 500`() {
        val ex = RuntimeException("Unexpected error")
        val response = handler.handleGeneric(ex)

        assert(response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR)
        assert(response.body?.status == 500)
    }
}
