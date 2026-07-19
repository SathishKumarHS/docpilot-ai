package com.docpilot.backend.document.service

import com.docpilot.backend.document.dto.DocumentResponse
import com.docpilot.backend.document.dto.UploadDocumentResponse
import com.docpilot.backend.document.entity.DocumentEntity
import com.docpilot.backend.document.model.Document
import com.docpilot.backend.document.repository.DocumentRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID

@Service
class DocumentService (
    private val validationService: DocumentValidationService,
    private val localStorageService: LocalStorageService,
    private val repository: DocumentRepository
) {
    private val documents = mutableListOf<Document>()

    fun upload(file: MultipartFile): UploadDocumentResponse {

        validationService.validate(file)

        localStorageService.save(file)

        val document = Document(
            id = UUID.randomUUID(),
            fileName = file.originalFilename ?: "unknown",
            uploadedAt = Instant.now()
        )

        repository.save(
            DocumentEntity(
                id = document.id,
                fileName = document.fileName,
                size = file.size,
                uploadedAt = document.uploadedAt
            )
        )

        return UploadDocumentResponse(
            id = document.id,
            fileName = document.fileName,
            size = file.size,
            uploadedAt = document.uploadedAt
        )
    }

    fun getAllDocuments(): List<DocumentResponse> {
        return repository.findAll()
            .map {
                DocumentResponse(
                    id = it.id,
                    fileName = it.fileName,
                    size = it.size,
                    uploadedAt = it.uploadedAt
                )
            }
    }
}