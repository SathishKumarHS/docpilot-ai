package com.docpilot.backend.document.scheduler

import com.docpilot.backend.document.service.DocumentService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DocumentCleanupJob(
    private val documentService: DocumentService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${docpilot.cleanup.cron}")
    fun cleanupExpiredDocuments() {
        log.info("Running expired document cleanup")
        documentService.cleanupExpiredDocuments()
    }
}
