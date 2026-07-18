package com.docpilot.backend.document.service

import com.docpilot.backend.document.dto.UploadDocumentRequest
import com.docpilot.backend.document.dto.UploadDocumentResponse
import com.docpilot.backend.document.model.Document
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID

@Service
class DocumentService (
    private val validationService: DocumentValidationService,
    private val localStorageService: LocalStorageService
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

        documents.add(document)

        return UploadDocumentResponse(
            id = document.id,
            fileName = document.fileName,
            size = file.size,
            uploadedAt = document.uploadedAt
        )
    }
}