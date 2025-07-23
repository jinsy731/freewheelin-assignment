package com.freewheelin.pulley.user.domain.model

import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.ValidationException
import java.time.LocalDateTime

/**
 * 사용자 도메인 엔티티 (Aggregate Root)
 *
 */
class User (
    val id: Long?,
    val username: String,
    val password: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null
) {
    companion object {
        private const val MIN_USERNAME_LENGTH = 3
        private const val MAX_USERNAME_LENGTH = 20
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MIN_NAME_LENGTH = 2
        private const val MAX_NAME_LENGTH = 50
        
        /**
         * 새로운 사용자 생성
         */
        fun create(
            username: String,
            password: String,
            name: String,
            email: String,
            role: UserRole
        ): User {
            validateUsername(username)
            validatePassword(password)
            validateName(name)
            validateEmail(email)
            
            return User(
                id = null,
                username = username.trim(),
                password = password, // 실제로는 암호화된 패스워드가 전달되어야 함
                name = name.trim(),
                email = email.trim().lowercase(),
                role = role,
                isActive = true,
                createdAt = LocalDateTime.now(),
                lastLoginAt = null
            )
        }
        
        /**
         * 기존 사용자 복원 (Repository에서 사용)
         */
        fun restore(
            id: Long,
            username: String,
            password: String,
            name: String,
            email: String,
            role: UserRole,
            isActive: Boolean,
            createdAt: LocalDateTime,
            lastLoginAt: LocalDateTime?
        ): User {
            return User(id, username, password, name, email, role, isActive, createdAt, lastLoginAt)
        }
        
        private fun validateUsername(username: String) {
            val trimmed = username.trim()
            when {
                trimmed.isBlank() -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "사용자명은 필수입니다",
                    mapOf("username" to "공백일 수 없습니다")
                )
                trimmed.length < MIN_USERNAME_LENGTH -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "사용자명이 너무 짧습니다",
                    mapOf("username" to "${MIN_USERNAME_LENGTH}자 이상이어야 합니다")
                )
                trimmed.length > MAX_USERNAME_LENGTH -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "사용자명이 너무 깁니다",
                    mapOf("username" to "${MAX_USERNAME_LENGTH}자 이하여야 합니다")
                )
                !trimmed.matches(Regex("^[a-zA-Z0-9._-]+$")) -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "사용자명에 허용되지 않은 문자가 포함되어 있습니다",
                    mapOf("username" to "영문, 숫자, ., _, - 만 사용 가능합니다")
                )
            }
        }
        
        private fun validatePassword(password: String) {
            when {
                password.isBlank() -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "비밀번호는 필수입니다",
                    mapOf("password" to "공백일 수 없습니다")
                )
                password.length < MIN_PASSWORD_LENGTH -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "비밀번호가 너무 짧습니다",
                    mapOf("password" to "${MIN_PASSWORD_LENGTH}자 이상이어야 합니다")
                )
            }
        }
        
        private fun validateName(name: String) {
            val trimmed = name.trim()
            when {
                trimmed.isBlank() -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "이름은 필수입니다",
                    mapOf("name" to "공백일 수 없습니다")
                )
                trimmed.length < MIN_NAME_LENGTH -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "이름이 너무 짧습니다",
                    mapOf("name" to "${MIN_NAME_LENGTH}자 이상이어야 합니다")
                )
                trimmed.length > MAX_NAME_LENGTH -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "이름이 너무 깁니다",
                    mapOf("name" to "${MAX_NAME_LENGTH}자 이하여야 합니다")
                )
            }
        }
        
        private fun validateEmail(email: String) {
            val trimmed = email.trim()
            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
            when {
                trimmed.isBlank() -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "이메일은 필수입니다",
                    mapOf("email" to "공백일 수 없습니다")
                )
                !trimmed.matches(emailRegex) -> throw ValidationException(
                    ErrorCode.VALIDATION_FAILED,
                    "올바른 이메일 형식이 아닙니다",
                    mapOf("email" to "유효한 이메일 주소를 입력하세요")
                )
            }
        }
    }
    
    /**
     * 특정 권한을 가지고 있는지 확인
     */
    fun hasAuthority(authority: String): Boolean {
        return isActive && role.hasAuthority(authority)
    }
    
    /**
     * 선생님인지 확인
     */
    fun isTeacher(): Boolean {
        return isActive && role == UserRole.TEACHER
    }
    
    /**
     * 학생인지 확인
     */
    fun isStudent(): Boolean {
        return isActive && role == UserRole.STUDENT
    }
    
    /**
     * 특정 리소스에 대한 소유권 확인
     */
    fun validateOwnership(ownerId: Long, resourceType: String, resourceId: Any) {
        if (!isActive) {
            throw AuthorizationException(
                ErrorCode.UNAUTHORIZED_ACCESS,
                "비활성화된 사용자입니다"
            )
        }
        
        if (this.id != ownerId) {
            throw AuthorizationException(
                ErrorCode.UNAUTHORIZED_ACCESS,
                this.id ?: 0L,
                resourceType,
                resourceId
            )
        }
    }
    
    /**
     * 로그인 시간 업데이트
     */
    fun updateLastLogin(): User {
        return User(
            id = this.id,
            username = this.username,
            password = this.password,
            name = this.name,
            email = this.email,
            role = this.role,
            isActive = this.isActive,
            createdAt = this.createdAt,
            lastLoginAt = LocalDateTime.now()
        )
    }
    
    /**
     * 사용자 비활성화
     */
    fun deactivate(): User {
        return User(
            id = this.id,
            username = this.username,
            password = this.password,
            name = this.name,
            email = this.email,
            role = this.role,
            isActive = false,
            createdAt = this.createdAt,
            lastLoginAt = this.lastLoginAt
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }
    
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
    
    override fun toString(): String {
        return "User(id=$id, username='$username', name='$name', role=$role, isActive=$isActive)"
    }
} 