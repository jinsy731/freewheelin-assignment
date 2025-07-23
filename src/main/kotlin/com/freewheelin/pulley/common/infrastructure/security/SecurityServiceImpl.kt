package com.freewheelin.pulley.common.infrastructure.security

import com.freewheelin.pulley.user.domain.model.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

/**
 * SecurityService 구현체
 * 
 * 기존 SecurityUtils의 로직을 Service로 이전하여 의존성 주입과 테스트 모킹이 가능하도록 구현
 */
@Service
class SecurityServiceImpl : SecurityService {
    
    override fun getCurrentUser(): User? {
        val authentication = getCurrentAuthentication()
        return if (authentication?.principal is CustomUserPrincipal) {
            (authentication.principal as CustomUserPrincipal).getUser()
        } else {
            null
        }
    }
    
    override fun getCurrentUserId(): Long? {
        return getCurrentUser()?.id
    }
    
    override fun getCurrentUsername(): String? {
        return getCurrentUser()?.username
    }
    
    override fun isCurrentUserTeacher(): Boolean {
        return getCurrentUser()?.isTeacher() ?: false
    }
    
    override fun isCurrentUserStudent(): Boolean {
        return getCurrentUser()?.isStudent() ?: false
    }
    
    override fun hasAuthority(authority: String): Boolean {
        return getCurrentUser()?.hasAuthority(authority) ?: false
    }
    
    override fun isResourceOwner(ownerId: Long): Boolean {
        val currentUserId = getCurrentUserId()
        return currentUserId != null && currentUserId == ownerId
    }
    
    override fun isAuthenticated(): Boolean {
        return getCurrentAuthentication() != null
    }
    
    override fun requireCurrentUser(): User {
        return getCurrentUser() 
            ?: throw IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.")
    }
    
    override fun requireCurrentUserId(): Long {
        return getCurrentUserId() 
            ?: throw IllegalStateException("인증된 사용자 ID를 찾을 수 없습니다.")
    }
    
    /**
     * 현재 Authentication 객체 반환
     */
    private fun getCurrentAuthentication(): Authentication? {
        val securityContext = SecurityContextHolder.getContext()
        val authentication = securityContext.authentication
        return if (authentication?.isAuthenticated == true && authentication.principal !is String) {
            authentication
        } else {
            null
        }
    }
} 