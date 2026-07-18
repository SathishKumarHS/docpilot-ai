package com.docpilot.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedDocumentTypeException::class)
    fun handleUnsupportedDocumentType(
        ex: UnsupportedDocumentTypeException
    ): ResponseEntity<ErrorResponse> {

        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(
                ErrorResponse(
                    status = 415,
                    message = ex.message ?: "Unsupported document type"
                )
            )
    }
}