package com.docpilot.backend

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@Tag("e2e")
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:postgresql://localhost:5432/docpilot",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.data.redis.host=localhost",
        "feature-flags.service-url=http://localhost:8090",
        "ai-worker.base-url=http://localhost:8000",
        "docpilot.jwt.secret=test-secret-that-is-long-enough-for-hs256-algorithm",
        "SERVICE_API_KEY=test-service-api-key",
    ],
)
class BackendApplicationTests {

    @Test
    fun contextLoads() {
    }

}
