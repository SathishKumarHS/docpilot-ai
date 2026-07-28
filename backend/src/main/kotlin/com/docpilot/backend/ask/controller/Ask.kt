package com.docpilot.backend.ask.controller

import com.docpilot.backend.aiworker.client.AiWorkerClient
import com.docpilot.backend.ask.dto.AskRequest
import com.docpilot.backend.ask.dto.AskResponse
import com.docpilot.backend.ask.dto.ChatMessageResponse
import com.docpilot.backend.ask.model.MessageRole
import com.docpilot.backend.ask.service.ChatHistoryService
import com.docpilot.backend.ask.service.QuestionLimitService
import com.docpilot.backend.auth.model.OwnerType
import com.docpilot.backend.auth.resolver.OwnerResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/ask")
class Ask(
    private val aiWorkerClient: AiWorkerClient,
    private val ownerResolver: OwnerResolver,
    private val questionLimitService: QuestionLimitService,
    private val chatHistoryService: ChatHistoryService,
) {

    @GetMapping("/history")
    fun getHistory(
        request: HttpServletRequest,
        @RequestParam("document_id") documentId: UUID? = null,
        @RequestParam(defaultValue = "50") limit: Int = 50,
    ): List<ChatMessageResponse> {
        val owner = ownerResolver.resolve(request)
        if (owner.ownerType == OwnerType.ANONYMOUS) return emptyList()
        return chatHistoryService.getHistory(owner.ownerId, documentId, PageRequest.of(0, limit))
            .map { ChatMessageResponse(role = it.role.name.lowercase(), content = it.content) }
    }

    @PostMapping
    fun ask(
        request: HttpServletRequest,
        @RequestBody @Valid askRequest: AskRequest,
    ): AskResponse {
        val owner = ownerResolver.resolve(request)
        val tier = owner.ownerType.name.lowercase()
        val isUser = owner.ownerType == OwnerType.USER
        questionLimitService.checkAndIncrement(owner.ownerId, tier)

        val documentId = askRequest.documentId

        val historyPairs = if (isUser) {
            chatHistoryService.getHistory(owner.ownerId, documentId, PageRequest.of(0, 50))
                .map { it.role.name.lowercase() to it.content }
        } else {
            emptyList()
        }

        if (isUser) {
            chatHistoryService.addMessage(owner.ownerId, documentId, MessageRole.USER, askRequest.question)
        }

        val response = aiWorkerClient.ask(askRequest, owner.ownerId, historyPairs)

        if (isUser) {
            chatHistoryService.addMessage(owner.ownerId, documentId, MessageRole.ASSISTANT, response.answer)
        }

        return AskResponse(answer = response.answer)
    }
}
