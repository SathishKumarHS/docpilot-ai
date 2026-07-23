package com.docpilot.backend.ask.service

import com.docpilot.backend.featureflag.service.FeatureFlagService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

@Service
class QuestionLimitService(
    private val redis: StringRedisTemplate,
    private val featureFlagService: FeatureFlagService,
) {
    fun checkAndIncrement(clientId: UUID, tier: String) {
        val limits = featureFlagService.getTierLimits(tier)
        val today = LocalDate.now(ZoneId.of("UTC"))
        val key = "questions:$today:$clientId"

        val count = redis.opsForValue().increment(key) ?: 1
        if (count == 1L) {
            val untilMidnight = Duration.between(
                LocalTime.now(ZoneId.of("UTC")),
                LocalTime.MAX,
            )
            redis.expire(key, untilMidnight)
        }

        if (count > limits.maxQuestionsPerDay) {
            throw IllegalArgumentException(
                "Daily question limit reached: max ${limits.maxQuestionsPerDay} questions per day"
            )
        }
    }
}
