package com.docpilot.backend.core.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "docpilot.ratelimit")
class RateLimitProperties {
    val limits: MutableMap<String, LimitConfig> = mutableMapOf()

    data class LimitConfig(
        val capacity: Long = 10,
        val period: Duration = Duration.ofMinutes(1),
    )
}
