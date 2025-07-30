package com.freewheelin.pulley.common.domain

import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.ErrorCode

/**
 * 소유권을 가진 엔티티를 나타내는 인터페이스
 */
interface Ownable<T> {
    /**
     * 특정 소유자가 이 리소스를 소유하고 있는지 확인
     */
    fun isOwnedBy(ownerId: T): Boolean
    
    /**
     * 리소스 식별자 반환
     */
    fun getResourceId(): Any
}

/**
 * 소유권 검증 확장 함수 (Long 타입 특화)
 */
fun Ownable<Long>.validateOwnership(ownerId: Long) {
    if (!isOwnedBy(ownerId)) {
        throw AuthorizationException(
            ErrorCode.UNAUTHORIZED_ACCESS,
            ownerId,
            this::class.simpleName ?: "Resource",
            getResourceId()
        )
    }
}