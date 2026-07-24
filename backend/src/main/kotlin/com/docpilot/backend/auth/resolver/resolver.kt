package com.docpilot.backend.auth.resolver

import com.docpilot.backend.auth.model.OwnerContext
import com.docpilot.backend.auth.model.OwnerType
import com.docpilot.backend.security.AnonymousSessionFilter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

interface OwnerResolver {
    fun resolve(request: HttpServletRequest): OwnerContext
}

@Component
class AnonymousOwnerResolver : OwnerResolver {

    override fun resolve(request: HttpServletRequest): OwnerContext {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth is UsernamePasswordAuthenticationToken) {
            return OwnerContext(
                ownerType = OwnerType.USER,
                ownerId = UUID.fromString(auth.principal.toString()),
            )
        }

        val clientId = request.getAttribute(AnonymousSessionFilter.ANON_CLIENT_ID_ATTR) as? UUID
            ?: throw IllegalArgumentException("Missing anonymous session")

        return OwnerContext(
            ownerType = OwnerType.ANONYMOUS,
            ownerId = clientId,
        )
    }
}
