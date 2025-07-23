package com.freewheelin.pulley.user.infrastructure.persistence

import com.freewheelin.pulley.user.domain.model.User
import com.freewheelin.pulley.user.domain.port.UserRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * 사용자 영속성 어댑터
 * 
 * 도메인 포트를 구현하여 실제 데이터베이스 접근을 담당합니다.
 */
@Repository
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    
    override fun save(user: User): User {
        val jpaEntity = UserJpaEntity.fromDomain(user)
        val savedEntity = userJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }
    
    override fun findById(id: Long): Optional<User> {
        return userJpaRepository.findById(id)
            .map { it.toDomain() }
    }

    override fun findByUsername(username: String): Optional<User> {
        return userJpaRepository.findByUsername(username)
            .map { it.toDomain() }
    }
}