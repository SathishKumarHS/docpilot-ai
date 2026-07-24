package com.docpilot.backend.document.service

import com.docpilot.backend.aiworker.client.AiWorkerClient
import com.docpilot.backend.aiworker.dto.IndexDocumentResponse
import com.docpilot.backend.anyNonNull
import com.docpilot.backend.eqNonNull
import com.docpilot.backend.auth.model.OwnerContext
import com.docpilot.backend.auth.model.OwnerType
import com.docpilot.backend.document.entity.DocumentChunkEntity
import com.docpilot.backend.document.entity.DocumentEntity
import com.docpilot.backend.document.model.DocumentChunk
import com.docpilot.backend.document.repository.DocumentChunkRepository
import com.docpilot.backend.document.repository.DocumentRepository
import com.docpilot.backend.document.storage.MinioService
import com.docpilot.backend.exception.NotFoundException
import com.docpilot.backend.featureflag.model.TierLimits
import com.docpilot.backend.featureflag.service.FeatureFlagService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*
import kotlin.test.*

class DocumentServiceTest {
    private val validationService = mock(DocumentValidationService::class.java)
    private val minioService = mock(MinioService::class.java)
    private val pdfExtractionService = mock(PdfExtractionService::class.java)
    private val documentChunkingService = mock(DocumentChunkingService::class.java)
    private val documentRepository = mock(DocumentRepository::class.java)
    private val documentChunkRepository = mock(DocumentChunkRepository::class.java)
    private val aiWorkerClient = mock(AiWorkerClient::class.java)
    private val featureFlagService = mock(FeatureFlagService::class.java)

    private val owner = OwnerContext(OwnerType.USER, UUID.randomUUID())
    private lateinit var service: DocumentService

    @BeforeEach
    fun setup() {
        service = DocumentService(
            validationService, minioService, pdfExtractionService,
            documentChunkingService, documentRepository, documentChunkRepository,
            aiWorkerClient, featureFlagService,
        )
    }

    @Test
    fun `upload saves document and indexes chunks`() {
        val file = mock(MultipartFile::class.java)
        `when`(file.originalFilename).thenReturn("test.pdf")
        `when`(file.size).thenReturn(1024L)
        `when`(featureFlagService.getTierLimits("user")).thenReturn(
            TierLimits(10, 30, 100L, 500, 100)
        )
        `when`(documentRepository.countByOwnerTypeAndOwnerId(OwnerType.USER, owner.ownerId)).thenReturn(0)
        `when`(minioService.save(anyNonNull(), anyNonNull())).thenReturn("test-key")
        `when`(pdfExtractionService.extractText(file)).thenReturn("document text")
        `when`(documentChunkingService.chunk("document text")).thenReturn(
            listOf(DocumentChunk(id = UUID.randomUUID(), chunkIndex = 0, content = "document text"))
        )
        `when`(aiWorkerClient.indexDocument(anyNonNull(), anyNonNull())).thenReturn(IndexDocumentResponse(1))

        val result = service.upload(file, owner)

        assertNotNull(result)
        assertEquals("test.pdf", result.fileName)
        assertEquals(1024L, result.size)
        verify(documentRepository).save(anyNonNull<DocumentEntity>())
    }

    @Test
    fun `upload throws when document limit reached`() {
        val file = mock(MultipartFile::class.java)
        `when`(featureFlagService.getTierLimits("user")).thenReturn(
            TierLimits(5, 30, 100L, 500, 100)
        )
        `when`(documentRepository.countByOwnerTypeAndOwnerId(OwnerType.USER, owner.ownerId)).thenReturn(5)

        assertFailsWith<IllegalArgumentException> {
            service.upload(file, owner)
        }
        verify(documentRepository, never()).save(anyNonNull<DocumentEntity>())
    }

    @Test
    fun `getAllDocuments returns page`() {
        val entity = DocumentEntity(
            id = UUID.randomUUID(),
            fileName = "test.pdf",
            size = 1024L,
            uploadedAt = Instant.now(),
            ownerType = OwnerType.USER,
            ownerId = owner.ownerId,
        )
        val page = PageImpl(listOf(entity))
        `when`(documentRepository.findByOwnerTypeAndOwnerId(OwnerType.USER, owner.ownerId, PageRequest.of(0, 20)))
            .thenReturn(page)

        val result = service.getAllDocuments(owner, PageRequest.of(0, 20))

        assertEquals(1, result.totalElements)
        assertEquals("test.pdf", result.content[0].fileName)
    }

    @Test
    fun `deleteDocument removes document`() {
        val docId = UUID.randomUUID()
        val entity = DocumentEntity(
            id = docId,
            fileName = "test.pdf",
            size = 1024L,
            uploadedAt = Instant.now(),
            ownerType = OwnerType.USER,
            ownerId = owner.ownerId,
            storageKey = "s3/key",
        )
        `when`(documentRepository.findByIdAndOwnerTypeAndOwnerId(docId, OwnerType.USER, owner.ownerId))
            .thenReturn(entity)

        service.deleteDocument(docId, owner)

        verify(aiWorkerClient).deleteDocument(docId, owner.ownerId)
        verify(minioService).delete("s3/key")
        verify(documentChunkRepository).deleteByDocumentId(docId)
        verify(documentRepository).delete(entity)
    }

    @Test
    fun `deleteDocument throws when not found`() {
        `when`(documentRepository.findByIdAndOwnerTypeAndOwnerId(anyNonNull(), anyNonNull(), anyNonNull())).thenReturn(null)

        assertFailsWith<NotFoundException> {
            service.deleteDocument(UUID.randomUUID(), owner)
        }
    }

    @Test
    fun `claimDocuments transfers ownership`() {
        val anonymousId = UUID.randomUUID()
        val docId = UUID.randomUUID()
        val doc = DocumentEntity(
            id = docId,
            fileName = "test.pdf",
            size = 1024L,
            uploadedAt = Instant.now(),
            ownerType = OwnerType.ANONYMOUS,
            ownerId = anonymousId,
        )
        val chunks = listOf(
            DocumentChunkEntity(
                id = UUID.randomUUID(),
                chunkIndex = 0,
                content = "text",
                document = doc,
            )
        )
        `when`(documentRepository.findAllByOwnerTypeAndOwnerId(OwnerType.ANONYMOUS, anonymousId))
            .thenReturn(listOf(doc))
        `when`(documentChunkRepository.findByDocumentId(docId)).thenReturn(chunks)

        val count = service.claimDocuments(anonymousId, owner)

        assertEquals(1, count)
        assertEquals(OwnerType.USER, doc.ownerType)
        assertEquals(owner.ownerId, doc.ownerId)
        verify(aiWorkerClient).deleteDocument(docId, anonymousId)
        verify(aiWorkerClient).indexDocument(anyNonNull(), eqNonNull(owner.ownerId))
    }

    @Test
    fun `cleanupExpiredDocuments removes expired`() {
        val docId = UUID.randomUUID()
        val oldDate = Instant.now().minusSeconds(86400 * 10)
        val doc = DocumentEntity(
            id = docId,
            fileName = "old.pdf",
            size = 1024L,
            uploadedAt = oldDate,
            ownerType = OwnerType.ANONYMOUS,
            ownerId = UUID.randomUUID(),
            storageKey = "s3/key",
        )
        `when`(documentRepository.findAllByUploadedAtBefore(anyNonNull())).thenReturn(listOf(doc))
        `when`(featureFlagService.getTierLimits("anonymous")).thenReturn(
            TierLimits(3, 1, 20L, 200, 50)
        )

        service.cleanupExpiredDocuments()

        verify(aiWorkerClient).deleteDocument(docId, doc.ownerId)
        verify(minioService).delete("s3/key")
        verify(documentRepository).delete(doc)
    }

    @Test
    fun `cleanupExpiredDocuments skips non-expired`() {
        val doc = DocumentEntity(
            id = UUID.randomUUID(),
            fileName = "new.pdf",
            size = 1024L,
            uploadedAt = Instant.now(),
            ownerType = OwnerType.USER,
            ownerId = UUID.randomUUID(),
        )
        `when`(documentRepository.findAllByUploadedAtBefore(anyNonNull())).thenReturn(listOf(doc))
        `when`(featureFlagService.getTierLimits("user")).thenReturn(
            TierLimits(10, 30, 100L, 500, 100)
        )

        service.cleanupExpiredDocuments()

        verify(documentRepository, never()).delete(anyNonNull())
    }
}
