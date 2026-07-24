package com.docpilot.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai-worker")
data class AiWorkerProperties(
    val serviceUrl: String
)