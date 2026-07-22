package com.docpilot.backend.featureflag

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:postgresql://localhost:5432/docpilot",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.data.redis.host=localhost",
        "feature-flags.service-url=http://localhost:8090",
    ],
)
@Tag("e2e")
class FeatureFlagE2ETest {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var web: WebTestClient

    @Autowired
    private lateinit var redis: StringRedisTemplate

    @BeforeEach
    fun setup() {
        web = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }

    @Test
    fun `flags cached in Redis`() {
        val hash = redis.opsForHash<String, String>().entries("feature:flags")
        assert(hash.isNotEmpty()) { "feature:flags hash is empty" }
        assert(hash.containsKey("limits.anonymous.max-documents")) { "missing limits.anonymous.max-documents" }
        assert(hash.containsKey("limits.user.max-documents")) { "missing limits.user.max-documents" }
        assert(hash["limits.anonymous.max-documents"] == "3") { "expected 3, got ${hash["limits.anonymous.max-documents"]}" }
        assert(hash["limits.user.max-documents"] == "20") { "expected 20, got ${hash["limits.user.max-documents"]}" }
    }

    @Test
    fun `backend health endpoint`() {
        web.get().uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
    }
}
