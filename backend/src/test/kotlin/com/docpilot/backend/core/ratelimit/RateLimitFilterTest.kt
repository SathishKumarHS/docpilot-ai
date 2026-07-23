package com.docpilot.backend.core.ratelimit

import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.ConsumptionProbe
import io.github.bucket4j.distributed.proxy.RecoveryStrategy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.PrintWriter
import java.time.Duration
import java.util.function.Supplier

@Tag("e2e")
class RateLimitFilterTest {

    private lateinit var filter: RateLimitFilter
    private lateinit var properties: RateLimitProperties
    private lateinit var proxyManager: LettuceBasedProxyManager<ByteArray>
    private lateinit var writer: PrintWriter

    @BeforeEach
    @Suppress("UNCHECKED_CAST")
    fun setup() {
        writer = PrintWriter(System.out)
        properties = RateLimitProperties()
        properties.limits["POST:/api/v1/ask"] = RateLimitProperties.LimitConfig(
            capacity = 2,
            period = Duration.ofMinutes(1),
        )

        proxyManager = Mockito.mock(
            LettuceBasedProxyManager::class.java,
            Mockito.RETURNS_DEEP_STUBS
        ) as LettuceBasedProxyManager<ByteArray>

        filter = RateLimitFilter(properties, proxyManager)
    }

    @Test
    fun `allows requests within limit`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.servletPath).thenReturn("/api/v1/ask")
        Mockito.`when`(request.method).thenReturn("POST")
        Mockito.`when`(request.getHeader("X-Client-Id")).thenReturn("client-1")
        Mockito.`when`(request.remoteAddr).thenReturn("127.0.0.1")

        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)

        val consumedProbe = ConsumptionProbe.consumed(1, 0)
        Mockito.`when`(
            proxyManager.builder()
                .withRecoveryStrategy(Mockito.any<RecoveryStrategy>())
                .build(Mockito.any<ByteArray>(), Mockito.any<Supplier<BucketConfiguration>>())
                .tryConsumeAndReturnRemaining(1)
        ).thenReturn(consumedProbe)

        repeat(2) {
            filter.doFilter(request, response, chain)
        }

        Mockito.verify(chain, Mockito.times(2)).doFilter(request, response)
    }

    @Test
    fun `returns 429 when limit exceeded`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.servletPath).thenReturn("/api/v1/ask")
        Mockito.`when`(request.method).thenReturn("POST")
        Mockito.`when`(request.getHeader("X-Client-Id")).thenReturn("client-2")
        Mockito.`when`(request.remoteAddr).thenReturn("127.0.0.1")

        val response = Mockito.mock(HttpServletResponse::class.java)
        Mockito.`when`(response.writer).thenReturn(writer)

        val chain = Mockito.mock(FilterChain::class.java)

        val consumedProbe = ConsumptionProbe.consumed(1, 0)
        val rejectedProbe = ConsumptionProbe.rejected(0, Duration.ofSeconds(30).toNanos(), Duration.ofSeconds(30).toNanos())

        Mockito.`when`(
            proxyManager.builder()
                .withRecoveryStrategy(Mockito.any<RecoveryStrategy>())
                .build(Mockito.any<ByteArray>(), Mockito.any<Supplier<BucketConfiguration>>())
                .tryConsumeAndReturnRemaining(1)
        ).thenReturn(consumedProbe, consumedProbe, rejectedProbe)

        repeat(2) { filter.doFilter(request, response, chain) }

        filter.doFilter(request, response, chain)

        Mockito.verify(response).setStatus(429)
    }

    @Test
    fun `different clients have independent limits`() {
        val chain = Mockito.mock(FilterChain::class.java)

        val consumedProbe = ConsumptionProbe.consumed(1, 0)
        Mockito.`when`(
            proxyManager.builder()
                .withRecoveryStrategy(Mockito.any<RecoveryStrategy>())
                .build(Mockito.any<ByteArray>(), Mockito.any<Supplier<BucketConfiguration>>())
                .tryConsumeAndReturnRemaining(1)
        ).thenReturn(consumedProbe)

        repeat(3) { i ->
            val request = Mockito.mock(HttpServletRequest::class.java)
            Mockito.`when`(request.servletPath).thenReturn("/api/v1/ask")
            Mockito.`when`(request.method).thenReturn("POST")
            Mockito.`when`(request.getHeader("X-Client-Id")).thenReturn("client-$i")
            Mockito.`when`(request.remoteAddr).thenReturn("127.0.0.1")

            val response = Mockito.mock(HttpServletResponse::class.java)
            Mockito.`when`(response.writer).thenReturn(writer)

            filter.doFilter(request, response, chain)
        }

        Mockito.verify(chain, Mockito.times(3)).doFilter(Mockito.any(), Mockito.any())
    }

    @Test
    fun `unconfigured endpoints pass through`() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.servletPath).thenReturn("/api/v1/health")
        Mockito.`when`(request.method).thenReturn("GET")

        val response = Mockito.mock(HttpServletResponse::class.java)
        val chain = Mockito.mock(FilterChain::class.java)

        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
        Mockito.verifyNoInteractions(response)
    }
}
