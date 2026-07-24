package com.docpilot.backend.featureflag.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "feature-flags")
class FeatureFlagProperties {
    var refreshIntervalMs: Long = 300000
    var serviceUrl: String = ""
    var redisKey: String = "feature:flags"
}
