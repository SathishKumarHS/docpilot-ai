package com.docpilot.backend.auth.resolver

import com.docpilot.backend.auth.model.OwnerContext
import com.docpilot.backend.auth.model.OwnerType
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.util.UUID

interface OwnerResolver {

    fun resolve(request: HttpServletRequest): OwnerContext

}

@Component
class AnonymousOwnerResolver : OwnerResolver {

    companion object {
        private const val CLIENT_ID_HEADER = "X-Client-Id"
    }

    override fun resolve(request: HttpServletRequest): OwnerContext {

        val clientId = request.getHeader(CLIENT_ID_HEADER)
            ?: throw IllegalArgumentException("Missing X-Client-Id header")

        return OwnerContext(
            ownerType = OwnerType.ANONYMOUS,
            ownerId = UUID.fromString(clientId)
        )
    }
}