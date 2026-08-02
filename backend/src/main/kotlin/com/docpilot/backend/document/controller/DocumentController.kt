package com.docpilot.backend.document.controller

import com.docpilot.backend.auth.resolver.OwnerResolver
import com.docpilot.backend.document.dto.DocumentResponse
import com.docpilot.backend.document.dto.UploadDocumentResponse
import com.docpilot.backend.document.service.DocumentService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/documents")
class DocumentController(
    private val ownerResolver: OwnerResolver,
    private val documentService: DocumentService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun upload(
        request: HttpServletRequest,
        @RequestParam("file") file: MultipartFile,
    ): UploadDocumentResponse {
        val owner = ownerResolver.resolve(request)
        log.info("upload request ownerType={} fileName={} size={}", owner.ownerType, file.originalFilename, file.size)
        return documentService.upload(file, owner)
    }

    @GetMapping
    fun getAllDocuments(
        request: HttpServletRequest,
        pageable: Pageable,
    ): Page<DocumentResponse> {
        val owner = ownerResolver.resolve(request)
        return documentService.getAllDocuments(owner, pageable)
    }

    @DeleteMapping("/{id}")
    fun deleteDocument(
        request: HttpServletRequest,
        @PathVariable id: UUID,
    ) {
        val owner = ownerResolver.resolve(request)
        log.info("delete request ownerType={} documentId={}", owner.ownerType, id)
        documentService.deleteDocument(id, owner)
    }
}
