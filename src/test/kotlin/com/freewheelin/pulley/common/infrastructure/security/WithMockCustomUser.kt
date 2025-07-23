package com.freewheelin.pulley.common.infrastructure.security

import org.springframework.security.test.context.support.WithSecurityContext
import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithMockCustomUser(
    val userId: Long = 1L,
    val email: String = "test@example.com",
    val username: String = "Test User",
    val roles: Array<String> = ["TEACHER"]
)