package com.freewheelin.pulley.user.infrastructure.persistence

import com.freewheelin.pulley.user.domain.model.User
import com.freewheelin.pulley.user.domain.model.UserRole
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 사용자 JPA 엔티티
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_username", columnList = "username", unique = true),
        Index(name = "idx_users_email", columnList = "email", unique = true)
    ]
)
@EntityListeners(AuditingEntityListener::class)
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "username", nullable = false, unique = true, length = 20)
    val username: String,

    @Column(name = "password", nullable = false, length = 255)
    val password: String,

    @Column(name = "name", nullable = false, length = 50)
    val name: String,

    @Column(name = "email", nullable = false, unique = true, length = 100)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    val role: UserRole,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null
) {
    
    /**
     * 도메인 모델로 변환
     */
    fun toDomain(): User {
        return User.restore(
            id = this.id!!,
            username = this.username,
            password = this.password,
            name = this.name,
            email = this.email,
            role = this.role,
            isActive = this.isActive,
            createdAt = this.createdAt,
            lastLoginAt = this.lastLoginAt
        )
    }
    
    companion object {
        /**
         * 도메인 모델에서 JPA 엔티티로 변환
         */
        fun fromDomain(user: User): UserJpaEntity {
            return UserJpaEntity(
                id = user.id,
                username = user.username,
                password = user.password,
                name = user.name,
                email = user.email,
                role = user.role,
                isActive = user.isActive,
                createdAt = user.createdAt,
                lastLoginAt = user.lastLoginAt
            )
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserJpaEntity) return false
        return id != null && id == other.id
    }
    
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
    
    override fun toString(): String {
        return "UserJpaEntity(id=$id, username='$username', name='$name', role=$role)"
    }
} 