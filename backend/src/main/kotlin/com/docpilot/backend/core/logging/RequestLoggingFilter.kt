package com.docpilot.backend.core.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(0)
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)
            ?: UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        MDC.put(MDC_KEY, requestId)
        request.setAttribute(REQUEST_ID_ATTR, requestId)

        val start = System.nanoTime()
        try {
            chain.doFilter(request, response)
        } finally {
            val durationMs = (System.nanoTime() - start) / 1_000_000
            val status = response.status
            val target = "${request.method} ${request.requestURI}"
            when {
                status >= 500 -> log.error("{} completed status={} duration={}ms", target, status, durationMs)
                status >= 400 -> log.warn("{} completed status={} duration={}ms", target, status, durationMs)
                else -> log.info("{} completed status={} duration={}ms", target, status, durationMs)
            }
            MDC.remove(MDC_KEY)
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI.startsWith("/actuator/")
    }

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val REQUEST_ID_ATTR = "requestId"
        const val MDC_KEY = "requestId"
    }
}
