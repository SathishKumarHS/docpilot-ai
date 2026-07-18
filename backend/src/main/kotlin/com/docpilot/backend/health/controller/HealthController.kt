package com.docpilot.backend.health.controller

import com.docpilot.backend.health.service.HealthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class HealthController (private val healthService: HealthService)  {

    @GetMapping("/health")
    fun health() = healthService.getHealth()
}