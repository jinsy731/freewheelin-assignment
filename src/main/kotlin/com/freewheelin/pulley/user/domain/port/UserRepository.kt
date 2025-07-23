package com.freewheelin.pulley.user.domain.port

import com.freewheelin.pulley.user.domain.model.User
import java.util.*

/**
 * 사용자 리포지토리 포트
 * 
 * 도메인 계층에서 정의하는 사용자 데이터 접근 추상화 인터페이스입니다.
 * Infrastructure 계층에서 구체적인 구현체를 제공합니다.
 */
interface UserRepository {
    
    /**
     * 사용자 저장
     */
    fun save(user: User): User
    
    /**
     * ID로 사용자 조회
     */
    fun findById(id: Long): Optional<User>

    /**
     * 사용자명으로 사용자 조회
     */
    fun findByUsername(username: String): Optional<User>
} 