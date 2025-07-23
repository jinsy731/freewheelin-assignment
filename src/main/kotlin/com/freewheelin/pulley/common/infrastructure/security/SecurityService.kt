package com.freewheelin.pulley.common.infrastructure.security

import com.freewheelin.pulley.user.domain.model.User

/**
 * Spring Security 관련 서비스 인터페이스
 * 
 * 현재 사용자 정보를 쉽게 가져올 수 있는 메서드들을 제공합니다.
 * 테스트에서 모킹이 가능하도록 인터페이스로 구현했습니다.
 */
interface SecurityService {
    
    /**
     * 현재 인증된 사용자 정보 반환
     */
    fun getCurrentUser(): User?
    
    /**
     * 현재 인증된 사용자 ID 반환
     */
    fun getCurrentUserId(): Long?
    
    /**
     * 현재 인증된 사용자명 반환
     */
    fun getCurrentUsername(): String?
    
    /**
     * 현재 사용자가 선생님인지 확인
     */
    fun isCurrentUserTeacher(): Boolean
    
    /**
     * 현재 사용자가 학생인지 확인
     */
    fun isCurrentUserStudent(): Boolean
    
    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인
     */
    fun hasAuthority(authority: String): Boolean
    
    /**
     * 현재 사용자가 리소스 소유자인지 확인
     */
    fun isResourceOwner(ownerId: Long): Boolean
    
    /**
     * 인증된 사용자인지 확인
     */
    fun isAuthenticated(): Boolean
    
    /**
     * 필수 사용자 정보 반환 (인증되지 않은 경우 예외 발생)
     */
    fun requireCurrentUser(): User
    
    /**
     * 필수 사용자 ID 반환 (인증되지 않은 경우 예외 발생)
     */
    fun requireCurrentUserId(): Long
} 