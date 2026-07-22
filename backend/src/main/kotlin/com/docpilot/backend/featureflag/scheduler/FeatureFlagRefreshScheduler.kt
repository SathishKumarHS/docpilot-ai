package com.docpilot.backend.featureflag.scheduler

import com.docpilot.backend.featureflag.client.FeatureFlagClient
import com.docpilot.backend.featureflag.config.FeatureFlagProperties
import com.docpilot.backend.featureflag.service.FeatureFlagService
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class FeatureFlagRefreshScheduler(
    private val client: FeatureFlagClient,
    private val service: FeatureFlagService,
    private val properties: FeatureFlagProperties,
) {
    @PostConstruct
    fun init() { refresh() }

    @Scheduled(
        fixedDelayString = "\${feature-flags.refresh-interval-ms:300000}",
        timeUnit = TimeUnit.MILLISECONDS,
    )
    fun refresh() {
        try {
            val flags = client.fetchFlags()
            service.refresh(flags)
        } catch (_: Exception) {
        }
    }
}
