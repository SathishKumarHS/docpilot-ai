package com.docpilot.backend.ask.controller

import com.docpilot.backend.aiworker.client.AiWorkerClient
import com.docpilot.backend.ask.dto.AskRequest
import com.docpilot.backend.ask.dto.AskResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/ask")
class Ask(
    private val aiWorkerClient: AiWorkerClient
) {

    @PostMapping
    fun ask(
        @RequestBody request: AskRequest,
        @RequestHeader("X-Client-Id") clientId: UUID,
    ): AskResponse {
        return aiWorkerClient.ask(request, clientId)
    }
}
