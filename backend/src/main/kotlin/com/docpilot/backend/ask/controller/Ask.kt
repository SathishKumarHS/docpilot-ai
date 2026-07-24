package com.docpilot.backend.ask.controller

import com.docpilot.backend.aiworker.client.AiWorkerClient
import com.docpilot.backend.ask.dto.AskRequest
import com.docpilot.backend.ask.dto.AskResponse
import com.docpilot.backend.ask.service.QuestionLimitService
import com.docpilot.backend.auth.resolver.OwnerResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/ask")
class Ask(
    private val aiWorkerClient: AiWorkerClient,
    private val ownerResolver: OwnerResolver,
    private val questionLimitService: QuestionLimitService,
) {

    @PostMapping
    fun ask(
        request: HttpServletRequest,
        @RequestBody @Valid askRequest: AskRequest,
    ): AskResponse {
        val owner = ownerResolver.resolve(request)
        val tier = owner.ownerType.name.lowercase()
        questionLimitService.checkAndIncrement(owner.ownerId, tier)
        return aiWorkerClient.ask(askRequest, owner.ownerId)
    }
}
