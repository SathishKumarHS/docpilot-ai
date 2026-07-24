package com.docpilot.backend.health.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.sql.Connection
import javax.sql.DataSource
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class HealthServiceTest {

    @Mock private lateinit var dataSource: DataSource
    @Mock private lateinit var redisConnectionFactory: RedisConnectionFactory
    @Mock private lateinit var sqlConnection: Connection
    @Mock private lateinit var redisConnection: RedisConnection

    @Test
    fun `health returns UP when everything is healthy`() {
        `when`(dataSource.connection).thenReturn(sqlConnection)
        `when`(redisConnectionFactory.connection).thenReturn(redisConnection)

        val result = HealthService(dataSource, redisConnectionFactory, "1.0.0").getHealth()

        assertEquals("UP", result["status"])
        assertEquals("DocPilot AI", result["service"])
        assertEquals("1.0.0", result["version"])
    }

    @Test
    fun `health returns DEGRADED when database is DOWN`() {
        `when`(dataSource.connection).thenThrow(RuntimeException("DB down"))
        `when`(redisConnectionFactory.connection).thenReturn(redisConnection)

        val result = HealthService(dataSource, redisConnectionFactory, "1.0.0").getHealth()

        assertEquals("DEGRADED", result["status"])
    }

    @Test
    fun `health returns DEGRADED when redis is DOWN`() {
        `when`(dataSource.connection).thenReturn(sqlConnection)
        `when`(redisConnectionFactory.connection).thenThrow(RuntimeException("Redis down"))

        val result = HealthService(dataSource, redisConnectionFactory, "1.0.0").getHealth()

        assertEquals("DEGRADED", result["status"])
    }
}
