package com.docpilot.backend

import org.mockito.Mockito

/**
 * Mockito.any() returns null, which is incompatible with Kotlin non-nullable types.
 * This helper casts the null to T so it can be used in stubbing expressions.
 */
@Suppress("UNCHECKED_CAST")
fun <T> anyNonNull(): T = Mockito.any<T>() as T

/**
 * Mockito.eq() wrapper for Kotlin non-nullable types.
 */
@Suppress("UNCHECKED_CAST")
fun <T> eqNonNull(value: T): T = Mockito.eq(value) as T
