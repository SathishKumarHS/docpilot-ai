package com.docpilot.backend.health.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class HealthService(
    private val dataSource: DataSource,
    private val redisConnectionFactory: RedisConnectionFactory,
    @Value("\${docpilot.version}") private val version: String,
) {

    fun getHealth(): Map<String, Any> {
        val db = checkDatabase()
        val redis = checkRedis()
        val overall = if (db == "UP" && redis == "UP") "UP" else "DEGRADED"

        return mapOf(
            "status" to overall,
            "service" to "DocPilot AI",
            "version" to version,
            "checks" to mapOf(
                "database" to db,
                "redis" to redis,
            ),
        )
    }

    private fun checkDatabase(): String = try {
        dataSource.connection.use { /* ping succeeded */ }
        "UP"
    } catch (e: Exception) {
        "DOWN"
    }

    private fun checkRedis(): String = try {
        redisConnectionFactory.connection.ping()
        "UP"
    } catch (e: Exception) {
        "DOWN"
    }
}