package com.docpilot.backend.featureflag.client

import com.docpilot.backend.featureflag.config.FeatureFlagProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class FeatureFlagClient(
    private val properties: FeatureFlagProperties,
    @Value("\${SERVICE_API_KEY}") serviceApiKey: String,
) {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl(properties.serviceUrl)
        .defaultHeader("X-Service-Key", serviceApiKey)
        .build()

    fun fetchFlags(): Map<String, Any> {
        return webClient
            .get()
            .uri("/flags")
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .block() ?: emptyMap()
    }
}
