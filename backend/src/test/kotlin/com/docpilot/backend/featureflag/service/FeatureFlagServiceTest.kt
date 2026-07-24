package com.docpilot.backend.featureflag.service

import com.docpilot.backend.featureflag.config.FeatureFlagProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration
import kotlin.test.assertEquals

class FeatureFlagServiceTest {
    private val redis = Mockito.mock(StringRedisTemplate::class.java)
    private lateinit var properties: FeatureFlagProperties
    private lateinit var service: FeatureFlagService

    @BeforeEach
    fun setup() {
        properties = FeatureFlagProperties()
        properties.redisKey = "feature:flags"
        properties.refreshIntervalMs = 300000
        service = FeatureFlagService(redis, properties)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `refresh stores flags in redis`() {
        val hashOps = Mockito.mock(HashOperations::class.java) as HashOperations<String, String, String>
        Mockito.`when`(redis.opsForHash<String, String>()).thenReturn(hashOps)

        val flags = mapOf("limits.anonymous.max-documents" to "5")
        service.refresh(flags)

        Mockito.verify(hashOps).putAll("feature:flags", flags)
        Mockito.verify(redis).expire("feature:flags", Duration.ofMillis(330000))
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `getTierLimits returns defaults when no flags set`() {
        val hashOps = Mockito.mock(HashOperations::class.java) as HashOperations<String, String, String>
        Mockito.`when`(redis.opsForHash<String, String>()).thenReturn(hashOps)
        Mockito.`when`(hashOps.get("feature:flags", "limits.anonymous.max-documents")).thenReturn(null)
        Mockito.`when`(hashOps.get("feature:flags", "limits.anonymous.expiration-days")).thenReturn(null)
        Mockito.`when`(hashOps.get("feature:flags", "limits.anonymous.max-file-size-mb")).thenReturn(null)
        Mockito.`when`(hashOps.get("feature:flags", "limits.anonymous.max-pages")).thenReturn(null)
        Mockito.`when`(hashOps.get("feature:flags", "limits.anonymous.max-questions-per-day")).thenReturn(null)

        val limits = service.getTierLimits("anonymous")

        assertEquals(3, limits.maxDocuments)
        assertEquals(1, limits.expirationDays)
        assertEquals(20L, limits.maxFileSizeMb)
        assertEquals(200, limits.maxPages)
        assertEquals(50, limits.maxQuestionsPerDay)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `getTierLimits returns configured values`() {
        val hashOps = Mockito.mock(HashOperations::class.java) as HashOperations<String, String, String>
        Mockito.`when`(redis.opsForHash<String, String>()).thenReturn(hashOps)
        Mockito.`when`(hashOps.get("feature:flags", "limits.premium.max-documents")).thenReturn("50")
        Mockito.`when`(hashOps.get("feature:flags", "limits.premium.expiration-days")).thenReturn("30")
        Mockito.`when`(hashOps.get("feature:flags", "limits.premium.max-file-size-mb")).thenReturn("100")
        Mockito.`when`(hashOps.get("feature:flags", "limits.premium.max-pages")).thenReturn("500")
        Mockito.`when`(hashOps.get("feature:flags", "limits.premium.max-questions-per-day")).thenReturn("200")

        val limits = service.getTierLimits("premium")

        assertEquals(50, limits.maxDocuments)
        assertEquals(30, limits.expirationDays)
        assertEquals(100L, limits.maxFileSizeMb)
        assertEquals(500, limits.maxPages)
        assertEquals(200, limits.maxQuestionsPerDay)
    }
}
