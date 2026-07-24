package com.docpilot.backend.featureflag.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TierLimitsTest {
    @Test
    fun `create tier limits`() {
        val limits = TierLimits(
            maxDocuments = 5,
            expirationDays = 7,
            maxFileSizeMb = 50L,
            maxPages = 100,
            maxQuestionsPerDay = 20,
        )

        assertEquals(5, limits.maxDocuments)
        assertEquals(7, limits.expirationDays)
        assertEquals(50L, limits.maxFileSizeMb)
        assertEquals(100, limits.maxPages)
        assertEquals(20, limits.maxQuestionsPerDay)
    }
}
