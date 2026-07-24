package com.docpilot.backend.featureflag.service

import com.docpilot.backend.featureflag.config.FeatureFlagProperties
import com.docpilot.backend.featureflag.model.TierLimits
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class FeatureFlagService(
    private val redis: StringRedisTemplate,
    private val properties: FeatureFlagProperties,
) {
    fun refresh(flags: Map<String, String>) {
        redis.opsForHash<String, String>().putAll(properties.redisKey, flags)
        redis.expire(properties.redisKey, Duration.ofMillis(properties.refreshIntervalMs + 30000))
    }

    private fun getRaw(key: String): String? =
        redis.opsForHash<String, String>().get(properties.redisKey, key)

    fun getInt(key: String): Int? = getRaw(key)?.toIntOrNull()

    fun getLong(key: String): Long? = getRaw(key)?.toLongOrNull()

    fun getBoolean(key: String): Boolean? = getRaw(key)?.let {
        when (it.lowercase()) { "true" -> true; "false" -> false; else -> null }
    }

    fun getInt(key: String, default: Int): Int = getInt(key) ?: default
    fun getLong(key: String, default: Long): Long = getLong(key) ?: default
    fun getString(key: String, default: String): String = getRaw(key) ?: default
    fun getBoolean(key: String, default: Boolean): Boolean = getBoolean(key) ?: default

    fun getTierLimits(tier: String): TierLimits = TierLimits(
        maxDocuments = getInt("limits.$tier.max-documents", 3),
        expirationDays = getInt("limits.$tier.expiration-days", 1),
        maxFileSizeMb = getLong("limits.$tier.max-file-size-mb", 20L),
        maxPages = getInt("limits.$tier.max-pages", 200),
        maxQuestionsPerDay = getInt("limits.$tier.max-questions-per-day", 50),
    )
}
