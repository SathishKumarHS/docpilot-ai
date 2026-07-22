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
                    // strip null bytes and control chars that PostgreSQL rejects in UTF-8 text columns
                    .replace("\u0000", "")
                    .replace(Regex("[\\u0001-\\u0008\\u000B\\u000C\\u000E-\\u001F\\uFFFE\\uFFFF]"), "")
            }
        }
    }
}