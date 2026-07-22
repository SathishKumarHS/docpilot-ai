package com.docpilot.backend.document.service

import com.docpilot.backend.auth.model.OwnerType
import com.docpilot.backend.exception.UnsupportedDocumentSizeException
import com.docpilot.backend.exception.UnsupportedDocumentTypeException
import com.docpilot.backend.featureflag.service.FeatureFlagService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class DocumentValidationService(
    private val featureFlagService: FeatureFlagService,
) {
    fun validate(file: MultipartFile, ownerType: OwnerType) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File cannot be empty")
        }
        if (file.contentType != "application/pdf") {
            throw UnsupportedDocumentTypeException("Only PDF files are supported")
        }

        val limits = featureFlagService.getTierLimits(ownerType.name.lowercase())
        val maxBytes = limits.maxFileSizeMb * 1024 * 1024

        if (file.size > maxBytes) {
            throw UnsupportedDocumentSizeException("File size cannot exceed ${limits.maxFileSizeMb} MB")
        }
    }
}
