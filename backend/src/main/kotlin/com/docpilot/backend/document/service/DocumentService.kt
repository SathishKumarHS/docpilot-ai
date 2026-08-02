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
import com.docpilot.backend.document.storage.MinioService
import com.docpilot.backend.exception.NotFoundException
import com.docpilot.backend.featureflag.service.FeatureFlagService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class DocumentService(
    private val validationService: DocumentValidationService,
    private val minioService: MinioService,
    private val pdfExtractionService: PdfExtractionService,
    private val documentChunkingService: DocumentChunkingService,
    private val documentRepository: DocumentRepository,
    private val documentChunkRepository: DocumentChunkRepository,
    private val aiWorkerClient: AiWorkerClient,
    private val featureFlagService: FeatureFlagService,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    @Transactional
    fun upload(file: MultipartFile, owner: OwnerContext): UploadDocumentResponse {
        validationService.validate(file, owner.ownerType)

        val limits = featureFlagService.getTierLimits(owner.ownerType.name.lowercase())
        val docCount = documentRepository.countByOwnerTypeAndOwnerId(owner.ownerType, owner.ownerId)
        if (docCount >= limits.maxDocuments) {
            throw IllegalArgumentException("Document limit reached: max ${limits.maxDocuments} documents")
        }

        val document = Document(
            id = UUID.randomUUID(),
            fileName = file.originalFilename ?: "unknown",
            uploadedAt = Instant.now()
        )

        val storageKey = minioService.save(file, document.id)

        val content = pdfExtractionService.extractText(file)

        val chunks = documentChunkingService.chunk(content)

        val documentEntity = DocumentEntity(
            id = document.id,
            fileName = document.fileName,
            size = file.size,
            uploadedAt = document.uploadedAt,
            ownerType = owner.ownerType,
            ownerId = owner.ownerId,
            storageKey = storageKey,
        )
        documentRepository.save(documentEntity)

        val entities = chunks.map {
            DocumentChunkEntity(
                id = it.id,
                chunkIndex = it.chunkIndex,
                content = it.content,
                document = documentEntity
            )
        }
        documentChunkRepository.saveAll(entities)

        val chunkRequests = chunks.map {
            ChunkRequest(chunkId = it.id, chunkIndex = it.chunkIndex, text = it.content)
        }

        aiWorkerClient.indexDocument(
            IndexDocumentRequest(documentId = document.id, chunks = chunkRequests),
            owner.ownerId
        )

        val summary = try {
            aiWorkerClient.summarizeDocument(chunkRequests, owner.ownerId)
        } catch (e: Exception) {
            log.warn("Failed to summarize document {}: {}", document.id, e.message)
            null
        }
        documentEntity.summary = summary
        documentRepository.save(documentEntity)

        return UploadDocumentResponse(
            id = document.id,
            fileName = document.fileName,
            size = file.size,
            uploadedAt = document.uploadedAt,
            summary = summary,
        )
    }

    fun getAllDocuments(owner: OwnerContext, pageable: Pageable): Page<DocumentResponse> {
        return documentRepository
            .findByOwnerTypeAndOwnerId(owner.ownerType, owner.ownerId, pageable)
            .map {
                DocumentResponse(id = it.id, fileName = it.fileName, size = it.size, uploadedAt = it.uploadedAt, summary = it.summary)
            }
    }

    @Transactional
    fun deleteDocument(documentId: UUID, owner: OwnerContext) {
        val document = documentRepository.findByIdAndOwnerTypeAndOwnerId(documentId, owner.ownerType, owner.ownerId)
            ?: throw NotFoundException("Document not found")

        aiWorkerClient.deleteDocument(document.id, document.ownerId)
        document.storageKey?.let { minioService.delete(it) }
        documentChunkRepository.deleteByDocumentId(document.id)
        documentRepository.delete(document)
    }

    @Transactional
    fun deleteDocument(document: DocumentEntity) {
        aiWorkerClient.deleteDocument(document.id, document.ownerId)
        document.storageKey?.let { minioService.delete(it) }
        documentChunkRepository.deleteByDocumentId(document.id)
        documentRepository.delete(document)
    }

    @Transactional
    fun claimDocuments(anonymousClientId: UUID, userOwner: OwnerContext): Int {
        val docs = documentRepository.findAllByOwnerTypeAndOwnerId(OwnerType.ANONYMOUS, anonymousClientId)
        docs.forEach { doc ->
            doc.ownerType = OwnerType.USER
            doc.ownerId = userOwner.ownerId
            documentRepository.save(doc)

            aiWorkerClient.deleteDocument(doc.id, anonymousClientId)
            val chunks = documentChunkRepository.findByDocumentId(doc.id)
            if (chunks.isNotEmpty()) {
                val request = IndexDocumentRequest(
                    documentId = doc.id,
                    chunks = chunks.map {
                        ChunkRequest(
                            chunkId = it.id,
                            chunkIndex = it.chunkIndex,
                            text = it.content,
                        )
                    },
                )
                aiWorkerClient.indexDocument(request, userOwner.ownerId)
            }
        }
        return docs.size
    }

    @Transactional
    fun cleanupExpiredDocuments() {
        val minLifetime = 1
        val threshold = Instant.now().minus(minLifetime.toLong(), ChronoUnit.DAYS)
        val candidates = documentRepository.findAllByUploadedAtBefore(threshold)
        if (candidates.isEmpty()) return

        log.info("Checking {} documents for expiry", candidates.size)
        candidates.forEach { document ->
            try {
                val tier = document.ownerType.name.lowercase()
                val limits = featureFlagService.getTierLimits(tier)
                val expiredAt = document.uploadedAt.plus(limits.expirationDays.toLong(), ChronoUnit.DAYS)
                if (Instant.now().isBefore(expiredAt)) return@forEach

                aiWorkerClient.deleteDocument(document.id, document.ownerId)
                document.storageKey?.let { minioService.delete(it) }
                documentChunkRepository.deleteByDocumentId(document.id)
                documentRepository.delete(document)
                log.info("Cleaned up expired document {}", document.id)
            } catch (e: Exception) {
                log.error("Failed to clean up document {}", document.id, e)
            }
        }
    }
}
