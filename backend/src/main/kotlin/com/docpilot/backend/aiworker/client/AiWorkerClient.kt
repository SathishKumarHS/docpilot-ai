package com.docpilot.backend.aiworker.client

import com.docpilot.backend.aiworker.dto.IndexDocumentRequest
import com.docpilot.backend.aiworker.dto.IndexDocumentResponse
import com.docpilot.backend.aiworker.exception.AiWorkerException
import com.docpilot.backend.ask.dto.AskRequest
import com.docpilot.backend.ask.dto.AskResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID

@Component
class AiWorkerClient(
    private val aiWorkerWebClient: WebClient,
) {

    fun indexDocument(
        request: IndexDocumentRequest,
    ): IndexDocumentResponse {
        return try {
            aiWorkerWebClient
                .post()
                .uri("/documents/index")
                .bodyValue(request)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    response.bodyToMono(String::class.java)
                        .map { body ->
                            println("AI Worker Error: $body")
                            AiWorkerException(body)
                        }
                }
                .bodyToMono(IndexDocumentResponse::class.java)
                .block()
                ?: throw AiWorkerException(
                    "AI Worker returned an empty response."
                )
        } catch (ex: Exception) {
            throw AiWorkerException(
                "Failed to index document using AI Worker.",
                ex
            )
        }
    }

    fun ask(request: AskRequest): AskResponse {
        return try {
            aiWorkerWebClient
                .post()
                .uri("/ask")
                .bodyValue(request)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    response.bodyToMono(String::class.java)
                        .map { body ->
                            println("AI Worker Error: $body")
                            AiWorkerException(body)
                        }
                }
                .bodyToMono(AskResponse::class.java)
                .block()
                ?: throw AiWorkerException("AI Worker returned an empty response.")
        } catch (ex: Exception) {
            throw AiWorkerException("Failed to ask AI Worker.", ex)
        }
    }

    fun deleteDocument(documentId: UUID) {
        aiWorkerWebClient.delete()
            .uri("/documents/{documentId}", documentId)
            .retrieve()
            .toBodilessEntity()
            .block()
    }
}