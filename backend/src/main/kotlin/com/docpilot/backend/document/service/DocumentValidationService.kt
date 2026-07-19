package com.docpilot.backend.document.service

import com.docpilot.backend.exception.UnsupportedDocumentSizeException
import com.docpilot.backend.exception.UnsupportedDocumentTypeException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class DocumentValidationService {

    private val maxFileSize = 10 * 1024 * 1024L // 10 MB

    fun validate(file: MultipartFile) {

        if (file.isEmpty) {
            throw IllegalArgumentException("File cannot be empty")
        }

        if (file.contentType != "application/pdf") {
            throw UnsupportedDocumentTypeException(
                "Only PDF files are supported"
            )
        }

        if (file.size > maxFileSize) {
            throw UnsupportedDocumentSizeException("File size cannot exceed 10 MB")
        }
    }
}