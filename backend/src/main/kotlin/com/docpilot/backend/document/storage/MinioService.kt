package com.docpilot.backend.document.storage

import com.docpilot.backend.config.MinioProperties
import io.minio.BucketExistsArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.http.Method
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class MinioService(
    private val properties: MinioProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client: MinioClient = MinioClient.builder()
        .endpoint(properties.url)
        .credentials(properties.accessKey, properties.secretKey)
        .build()

    @PostConstruct
    fun init() {
        try {
            val exists = client.bucketExists(BucketExistsArgs.builder().bucket(properties.bucket).build())
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket).build())
                log.info("Created MinIO bucket: {}", properties.bucket)
            }
        } catch (e: Exception) {
            log.warn("Failed to initialize MinIO bucket: {}", e.message)
        }
    }

    fun save(file: MultipartFile, documentId: UUID): String {
        val objectName = "${documentId}/${file.originalFilename ?: "document.pdf"}"
        client.putObject(
            PutObjectArgs.builder()
                .bucket(properties.bucket)
                .`object`(objectName)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType ?: "application/octet-stream")
                .build()
        )
        return objectName
    }

    fun delete(objectName: String) {
        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(properties.bucket)
                .`object`(objectName)
                .build()
        )
    }
}
