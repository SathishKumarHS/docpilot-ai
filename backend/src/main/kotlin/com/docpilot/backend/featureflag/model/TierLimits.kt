package com.docpilot.backend.featureflag.model

data class TierLimits(
    val maxDocuments: Int,
    val expirationDays: Int,
    val maxFileSizeMb: Long,
    val maxPages: Int,
    val maxQuestionsPerDay: Int,
)
