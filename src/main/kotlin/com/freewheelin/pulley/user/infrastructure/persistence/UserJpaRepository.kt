package com.freewheelin.pulley.user.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * 사용자 JPA Repository
 */
interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    
    /**
     * 사용자명으로 사용자 조회
     */
    fun findByUsername(username: String): Optional<UserJpaEntity>
}