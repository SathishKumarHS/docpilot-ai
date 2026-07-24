package com.docpilot.backend.aiworker.config

import com.docpilot.backend.config.AiWorkerProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun aiWorkerWebClient(
        properties: AiWorkerProperties,
        @Value("\${SERVICE_API_KEY}") serviceApiKey: String,
    ): WebClient {

        return WebClient.builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader("X-Service-Key", serviceApiKey)
            .build()
    }
}