package com.docpilot.backend.document.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PdfExtractionService {

    fun extractText(file: MultipartFile): String {
        file.inputStream.use { inputStream ->
            Loader.loadPDF(inputStream.readBytes()).use { document ->
                val stripper = PDFTextStripper()
                return stripper.getText(document).trim()
            }
        }
    }
}