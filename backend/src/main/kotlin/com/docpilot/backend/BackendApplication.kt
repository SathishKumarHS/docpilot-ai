package com.docpilot.backend

import com.docpilot.backend.config.AiWorkerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(
    AiWorkerProperties::class
)

@SpringBootApplication
class BackendApplication

fun main(args: Array<String>) {
    println("DATABASE_URL = ${System.getenv("DATABASE_URL")}")
    println("AI_WORKER_BASE_URL = ${System.getenv("AI_WORKER_BASE_URL")}")
    runApplication<BackendApplication>(*args)
}
