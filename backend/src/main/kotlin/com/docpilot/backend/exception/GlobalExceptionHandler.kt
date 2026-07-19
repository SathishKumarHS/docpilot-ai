package com.docpilot.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedDocumentTypeException::class)
    fun handleUnsupportedDocumentSize(
        ex: UnsupportedDocumentTypeException
    ): ResponseEntity<ErrorResponse> {

        return ResponseEntity
            .status(HttpStatus.CONTENT_TOO_LARGE)
            .body(
                ErrorResponse(
                    status = 415,
                    message = ex.message ?: "Unsupported document type"
                )
            )
    }

    @ExceptionHandler(UnsupportedDocumentSizeException::class)
    fun handleUnsupportedDocumentType(
        ex: UnsupportedDocumentSizeException
    ): ResponseEntity<ErrorResponse> {

        return ResponseEntity
            .status(HttpStatus.CONTENT_TOO_LARGE)
            .body(
                ErrorResponse(
                    status = 415,
                    message = ex.message ?: "Unsupported document size"
                )
            )
    }
}