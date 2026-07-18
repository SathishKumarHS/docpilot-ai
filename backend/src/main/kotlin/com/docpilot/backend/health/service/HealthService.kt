package com.docpilot.backend.health.service

import org.springframework.stereotype.Service

@Service
class HealthService {

    fun getHealth(): Map<String, String> {

        return mapOf(
            "status" to "UP",
            "service" to "DocPilot AI",
            "version" to "1.0.0"
        )

    }

}