package com.docpilot.backend.document.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Service
class LocalStorageService {

    private val uploadDirectory = Path.of("uploads")

    init {
        Files.createDirectories(uploadDirectory)
    }

    fun save(file: MultipartFile): String {

        val filePath = uploadDirectory.resolve(file.originalFilename!!)

        Files.copy(
            file.inputStream,
            filePath,
            StandardCopyOption.REPLACE_EXISTING
        )

        return filePath.toString()
    }
}