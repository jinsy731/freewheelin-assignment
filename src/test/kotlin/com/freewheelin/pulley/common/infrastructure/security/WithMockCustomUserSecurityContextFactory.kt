package com.freewheelin.pulley.common.infrastructure.security

import com.freewheelin.pulley.user.domain.model.User
import com.freewheelin.pulley.user.domain.model.UserRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.time.LocalDateTime

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {

    override fun createSecurityContext(annotation: WithMockCustomUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()

        val principal = CustomUserPrincipal(
            user = User(
                id = annotation.userId,
                username = annotation.username,
                password = "",
                name = annotation.username,
                email = annotation.email,
                role = UserRole.valueOf(annotation.roles.first()),
                isActive = true,
                createdAt = LocalDateTime.now()
            ),
        )

        val auth = UsernamePasswordAuthenticationToken(
            principal,
            "",
            listOf(SimpleGrantedAuthority("ROLE_${annotation.roles.first()}"))
        )

        context.authentication = auth
        return context
    }
}