package com.docpilot.backend.document.scheduler

import com.docpilot.backend.document.service.DocumentService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class DocumentCleanupJobTest {

    @Mock private lateinit var documentService: DocumentService

    @Test
    fun `cleanupExpiredDocuments delegates`() {
        val job = DocumentCleanupJob(documentService)
        job.cleanupExpiredDocuments()

        verify(documentService).cleanupExpiredDocuments()
    }
}
