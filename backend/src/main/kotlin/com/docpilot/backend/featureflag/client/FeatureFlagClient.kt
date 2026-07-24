package com.docpilot.backend.featureflag.client

import com.docpilot.backend.featureflag.config.FeatureFlagProperties
import featureflag.FeatureFlagServiceGrpc
import featureflag.Featureflag
import io.grpc.ManagedChannelBuilder
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class FeatureFlagClient(
    private val properties: FeatureFlagProperties,
    @Value("\${SERVICE_API_KEY}") serviceApiKey: String,
) {
    private val channel = ManagedChannelBuilder
        .forTarget(properties.serviceUrl)
        .usePlaintext()
        .build()

    private val stub = FeatureFlagServiceGrpc.newBlockingStub(channel)
        .withInterceptors(MetadataInterceptor("x-service-key", serviceApiKey))

    fun fetchFlags(): Map<String, String> {
        val response = stub.getFlags(Featureflag.GetFlagsRequest.getDefaultInstance())
        return response.flagsMap
    }

    @PreDestroy
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}
