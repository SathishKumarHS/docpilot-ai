package com.docpilot.backend.core.ratelimit

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class BucketConfig {

    @Bean(destroyMethod = "shutdown")
    fun bucket4jRedisClient(
        @Value("\${spring.data.redis.host}") host: String,
        @Value("\${spring.data.redis.port}") port: Int,
    ): RedisClient {
        return RedisClient.create("redis://$host:$port")
    }

    @Bean
    fun proxyManager(
        redisClient: RedisClient,
    ): LettuceBasedProxyManager<ByteArray> {
        return LettuceBasedProxyManager.builderFor(redisClient)
            .withExpirationStrategy(
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10))
            )
            .build()
    }
}
