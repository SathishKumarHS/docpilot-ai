package com.docpilot.backend.document.service

import com.docpilot.backend.aiworker.client.AiWorkerClient
import com.docpilot.backend.aiworker.dto.ChunkRequest
import com.docpilot.backend.aiworker.dto.IndexDocumentRequest
import com.docpilot.backend.auth.model.OwnerContext
import com.docpilot.backend.auth.model.OwnerType
import com.docpilot.backend.document.dto.DocumentResponse
import com.docpilot.backend.document.dto.UploadDocumentResponse
import com.docpilot.backend.document.entity.DocumentChunkEntity
import com.docpilot.backend.document.entity.DocumentEntity
import com.docpilot.backend.document.model.Document
import com.docpilot.backend.document.repository.DocumentChunkRepository
import com.docpilot.backend.document.repository.DocumentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class DocumentService (
    private val validationService: DocumentValidationService,
    private val localStorageService: LocalStorageService,
    private val pdfExtractionService: PdfExtractionService,
    private val documentChunkingService: DocumentChunkingService,
    private val documentRepository: DocumentRepository,
    private val documentChunkRepository: DocumentChunkRepository,
    private val aiWorkerClient: AiWorkerClient

) {

    @Transactional
    fun upload(file: MultipartFile, owner: OwnerContext): UploadDocumentResponse {

        validationService.validate(file)

        localStorageService.save(file)

        val document = Document(
            id = UUID.randomUUID(),
            fileName = file.originalFilename ?: "unknown",
            uploadedAt = Instant.now()
        )

        val existingDocument = documentRepository.findByOwnerTypeAndOwnerId(
            owner.ownerType,
            owner.ownerId
        )

        if (existingDocument != null) {
            deleteDocument(existingDocument)
        }

        val content = pdfExtractionService.extractText(file)

        val chunks = documentChunkingService.chunk(content)

        val documentEntity = DocumentEntity(
            id = document.id,
            fileName = document.fileName,
            size = file.size,
            uploadedAt = document.uploadedAt,
            ownerType = owner.ownerType,
            ownerId = owner.ownerId,
            expiresAt = when (owner.ownerType) {
                OwnerType.ANONYMOUS -> Instant.now().plus(1, ChronoUnit.DAYS)
                OwnerType.USER -> Instant.now().plus(7, ChronoUnit.DAYS)
            }
        )
        documentRepository.save(
            documentEntity
        )

        val entities = chunks.map {
            DocumentChunkEntity(
                id = it.id,
                chunkIndex = it.chunkIndex,
                content = it.content,
                document = documentEntity
            )
        }

        documentChunkRepository.saveAll(entities)

        val request = IndexDocumentRequest(
            documentId = document.id,
            chunks = chunks.map {
                ChunkRequest(
                    chunkId = it.id,
                    chunkIndex = it.chunkIndex,
                    text = it.content,
                )
            }
        )

        aiWorkerClient.indexDocument(request)



        return UploadDocumentResponse(
            id = document.id,
            fileName = document.fileName,
            size = file.size,
            uploadedAt = document.uploadedAt
        )
    }

    fun getAllDocuments(): List<DocumentResponse> {
        return documentRepository.findAll()
            .map {
                DocumentResponse(
                    id = it.id,
                    fileName = it.fileName,
                    size = it.size,
                    uploadedAt = it.uploadedAt
                )
            }
    }

    @Transactional
    fun deleteDocument(document: DocumentEntity) {
        // Step 1: Delete vectors
        aiWorkerClient.deleteDocument(document.id)

        // Step 2: Delete chunks
        documentChunkRepository.deleteByDocumentId(document.id)

        // Step 3: Delete document
        documentRepository.delete(document)
    }

}