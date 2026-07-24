package com.docpilot.backend.ask.service

import com.docpilot.backend.featureflag.model.TierLimits
import com.docpilot.backend.featureflag.service.FeatureFlagService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.*
import kotlin.test.assertFailsWith

class QuestionLimitServiceTest {
    private val redis = mock(StringRedisTemplate::class.java)
    private val featureFlagService = mock(FeatureFlagService::class.java)
    @Suppress("UNCHECKED_CAST")
    private val valueOps = mock(ValueOperations::class.java) as ValueOperations<String, String>

    @Test
    fun `allows requests within limit`() {
        val clientId = UUID.randomUUID()
        `when`(featureFlagService.getTierLimits("free")).thenReturn(
            TierLimits(3, 1, 20L, 200, 10)
        )
        `when`(redis.opsForValue()).thenReturn(valueOps)
        `when`(valueOps.increment(anyString())).thenReturn(1L)

        val service = QuestionLimitService(redis, featureFlagService)
        service.checkAndIncrement(clientId, "free")

        verify(valueOps).increment(anyString())
    }

    @Test
    fun `throws when limit exceeded`() {
        val clientId = UUID.randomUUID()
        `when`(featureFlagService.getTierLimits("free")).thenReturn(
            TierLimits(3, 1, 20L, 200, 2)
        )
        `when`(redis.opsForValue()).thenReturn(valueOps)
        `when`(valueOps.increment(anyString())).thenReturn(3L)

        val service = QuestionLimitService(redis, featureFlagService)

        assertFailsWith<IllegalArgumentException> {
            service.checkAndIncrement(clientId, "free")
        }
    }
}
