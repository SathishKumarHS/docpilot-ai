package com.docpilot.backend.aiworker.client

import aiworker.AiWorkerServiceGrpc
import aiworker.Aiworker
import com.docpilot.backend.ask.dto.AskRequest
import com.docpilot.backend.ask.dto.AskResponse
import com.docpilot.backend.aiworker.dto.ChunkRequest
import com.docpilot.backend.aiworker.dto.IndexDocumentRequest
import com.docpilot.backend.aiworker.dto.IndexDocumentResponse
import com.docpilot.backend.aiworker.exception.AiWorkerException
import com.docpilot.backend.config.AiWorkerProperties
import com.docpilot.backend.featureflag.client.MetadataInterceptor
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID
import java.util.concurrent.TimeUnit

@Component
class AiWorkerClient(
    private val properties: AiWorkerProperties,
    @Value("\${SERVICE_API_KEY}") serviceApiKey: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val channel = ManagedChannelBuilder
        .forTarget(properties.serviceUrl)
        .usePlaintext()
        .build()

    private val stub = AiWorkerServiceGrpc.newBlockingStub(channel)
        .withInterceptors(
            MetadataInterceptor("x-service-key", serviceApiKey),
        )

    fun indexDocument(
        request: IndexDocumentRequest,
        clientId: UUID,
    ): IndexDocumentResponse {
        return try {
            val protoRequest = Aiworker.IndexDocumentRequest.newBuilder()
                .setDocumentId(request.documentId.toString())
                .addAllChunks(request.chunks.map { it.toProto() })
                .build()

            val response = stub.withInterceptors(
                MetadataInterceptor("x-client-id", clientId.toString()),
            ).indexDocument(protoRequest)

            IndexDocumentResponse(indexedChunks = response.indexedChunks)
        } catch (e: StatusRuntimeException) {
            log.error("AI Worker gRPC error: {}", e.status.description)
            throw AiWorkerException(e.status.description ?: "Unknown gRPC error", e)
        }
    }

    fun ask(
        request: AskRequest,
        clientId: UUID,
        chatHistory: List<Pair<String, String>> = emptyList(),
    ): AskResponse {
        return try {
            val protoRequest = Aiworker.AskRequest.newBuilder()
                .setQuestion(request.question)
                .apply { request.documentId?.let { setDocumentId(it.toString()) } }
                .addAllChatHistory(chatHistory.map { (role, content) ->
                    Aiworker.ChatMessage.newBuilder()
                        .setRole(
                            when (role) {
                                "user" -> Aiworker.MessageRole.USER
                                "assistant" -> Aiworker.MessageRole.ASSISTANT
                                else -> error("Unknown role: $role")
                            }
                        )
                        .setContent(content)
                        .build()
                })
                .build()

            val response = stub.withInterceptors(
                MetadataInterceptor("x-client-id", clientId.toString()),
            ).ask(protoRequest)

            AskResponse(answer = response.answer)
        } catch (e: StatusRuntimeException) {
            log.error("AI Worker gRPC error: {}", e.status.description)
            throw AiWorkerException(e.status.description ?: "Unknown gRPC error", e)
        }
    }

    fun askStream(
        request: AskRequest,
        clientId: UUID,
        chatHistory: List<Pair<String, String>> = emptyList(),
        onToken: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val protoRequest = Aiworker.AskRequest.newBuilder()
            .setQuestion(request.question)
            .apply { request.documentId?.let { setDocumentId(it.toString()) } }
            .addAllChatHistory(chatHistory.map { (role, content) ->
                Aiworker.ChatMessage.newBuilder()
                    .setRole(
                        when (role) {
                            "user" -> Aiworker.MessageRole.USER
                            "assistant" -> Aiworker.MessageRole.ASSISTANT
                            else -> error("Unknown role: $role")
                        }
                    )
                    .setContent(content)
                    .build()
            })
            .build()

        try {
            val iterator = stub.withInterceptors(
                MetadataInterceptor("x-client-id", clientId.toString()),
            ).askStream(protoRequest)

            iterator.forEach { response ->
                if (response.token.isNotEmpty()) {
                    onToken(response.token)
                }
                if (response.done) {
                    onComplete()
                }
            }
        } catch (e: StatusRuntimeException) {
            log.error("AI Worker gRPC stream error: {}", e.status.description)
            onError(AiWorkerException(e.status.description ?: "Unknown gRPC stream error", e))
        } catch (e: Exception) {
            log.error("AI Worker gRPC stream error", e)
            onError(e)
        }
    }

    fun deleteDocument(
        documentId: UUID,
        clientId: UUID,
    ) {
        try {
            val protoRequest = Aiworker.DeleteDocumentRequest.newBuilder()
                .setDocumentId(documentId.toString())
                .build()

            stub.withInterceptors(
                MetadataInterceptor("x-client-id", clientId.toString()),
            ).deleteDocument(protoRequest)
        } catch (e: StatusRuntimeException) {
            log.error("AI Worker gRPC error: {}", e.status.description)
            throw AiWorkerException(e.status.description ?: "Unknown gRPC error", e)
        }
    }

    @PreDestroy
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

private fun ChunkRequest.toProto(): Aiworker.Chunk {
    return Aiworker.Chunk.newBuilder()
        .setChunkId(chunkId.toString())
        .setChunkIndex(chunkIndex)
        .setText(text)
        .build()
}
