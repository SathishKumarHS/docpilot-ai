package com.docpilot.backend.document.controller

import com.docpilot.backend.auth.resolver.OwnerResolver
import com.docpilot.backend.document.dto.DocumentResponse
import com.docpilot.backend.document.dto.UploadDocumentResponse
import com.docpilot.backend.document.service.DocumentService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/documents")
class DocumentController(
    private val ownerResolver: OwnerResolver,
    private val documentService: DocumentService
) {

    @PostMapping
    fun upload(
        request: HttpServletRequest,
        @RequestHeader("X-Client-Id") clientId: UUID?,
        @RequestParam("file") file: MultipartFile
    ): UploadDocumentResponse {
        val owner = ownerResolver.resolve(request)
        return documentService.upload(file, owner)

    }

    @GetMapping
    fun getAllDocuments(): List<DocumentResponse> {
        return documentService.getAllDocuments()
    }
}