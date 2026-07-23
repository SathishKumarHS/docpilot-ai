package com.docpilot.backend.core.ratelimit

import com.docpilot.backend.core.ratelimit.RateLimitProperties.LimitConfig
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.RecoveryStrategy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
@Order(1)
class RateLimitFilter(
    private val properties: RateLimitProperties,
    private val proxyManager: LettuceBasedProxyManager<ByteArray>,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val pathMatcher = AntPathMatcher()
    private val configurations = ConcurrentHashMap<String, BucketConfiguration>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val path = request.servletPath
        val method = request.method
        val candidate = "$method:$path"

        val pattern = properties.limits.keys.firstOrNull { pattern ->
            pathMatcher.match(pattern, candidate)
        } ?: return chain.doFilter(request, response)

        val clientId = request.getHeader("X-Client-Id")
            ?: request.remoteAddr
            ?: "unknown"

        val limit = properties.limits[pattern]!!
        val config = configurations.getOrPut(pattern) { newConfiguration(limit) }
        val bucketKey = "ratelimit:$clientId:$pattern"

        val bucket = proxyManager.builder()
            .withRecoveryStrategy(RecoveryStrategy.RECONSTRUCT)
            .build(bucketKey.encodeToByteArray()) { config }

        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            chain.doFilter(request, response)
        } else {
            val waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.nanosToWaitForRefill)
            log.warn("Rate limit exceeded for client={} pattern={}", clientId, pattern)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.setHeader("Retry-After", waitSeconds.toString())
            response.contentType = "application/json"
            response.writer.write(
                """{"error":"Too many requests. Retry after ${waitSeconds}s","status":429}"""
            )
        }
    }

    private fun newConfiguration(limit: LimitConfig): BucketConfiguration = BucketConfiguration.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(limit.capacity)
                .refillGreedy(limit.capacity, limit.period)
                .build()
        )
        .build()
}
