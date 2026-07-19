package com.docpilot.backend.document.controller

import com.docpilot.backend.document.dto.DocumentResponse
import com.docpilot.backend.document.dto.UploadDocumentResponse
import com.docpilot.backend.document.service.DocumentService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/documents")
class DocumentController(
    private val documentService: DocumentService
) {

    @PostMapping
    fun upload(
        @RequestParam("file")
        file: MultipartFile
    ): UploadDocumentResponse {

        return documentService.upload(file)

    }

    @GetMapping
    fun getAllDocuments(): List<DocumentResponse> {
        return documentService.getAllDocuments()
    }
}