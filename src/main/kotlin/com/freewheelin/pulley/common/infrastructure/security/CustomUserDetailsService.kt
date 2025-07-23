package com.freewheelin.pulley.common.infrastructure.security

import com.freewheelin.pulley.user.domain.model.User
import com.freewheelin.pulley.user.domain.port.UserRepository
import mu.KotlinLogging
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Spring Security UserDetailsService 구현체
 * 
 * 사용자 인증을 위해 데이터베이스에서 사용자 정보를 조회합니다.
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    override fun loadUserByUsername(username: String): UserDetails {
        logger.debug { "사용자 인증 정보 조회 시작 - username: $username" }
        
        try {
            val user = userRepository.findByUsername(username)
                .orElseThrow { 
                    logger.warn { "사용자 인증 실패 - 존재하지 않는 사용자: $username" }
                    UsernameNotFoundException("사용자를 찾을 수 없습니다: $username") 
                }
            
            if (!user.isActive) {
                logger.warn { "사용자 인증 실패 - 비활성 사용자: $username" }
                throw UsernameNotFoundException("비활성화된 사용자입니다: $username")
            }
            
            logger.debug { 
                "사용자 인증 정보 조회 성공 - username: $username, userId: ${user.id}, " +
                "role: ${user.role}, active: ${user.isActive}" 
            }
            
            return CustomUserPrincipal(user)
            
        } catch (e: Exception) {
            logger.warn(e) { "사용자 인증 정보 조회 실패 - username: $username, error: ${e.message}" }
            throw e
        }
    }
}

/**
 * Spring Security UserDetails 구현체
 * 
 * 도메인의 User 엔티티를 Spring Security가 이해할 수 있는 형태로 변환합니다.
 */
class CustomUserPrincipal(
    private val user: User
) : UserDetails {
    
    fun getUser(): User = user
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return user.role.getSecurityAuthorities()
            .map { SimpleGrantedAuthority(it) }
    }
    
    override fun getPassword(): String = user.password
    
    override fun getUsername(): String = user.username
    
    override fun isAccountNonExpired(): Boolean = user.isActive
    
    override fun isAccountNonLocked(): Boolean = user.isActive
    
    override fun isCredentialsNonExpired(): Boolean = true
    
    override fun isEnabled(): Boolean = user.isActive
    
    /**
     * 사용자 ID 반환
     */
    fun getId(): Long? = user.id
    
    /**
     * 사용자 이름 반환
     */
    fun getName(): String = user.name
    
    /**
     * 사용자 이메일 반환
     */
    fun getEmail(): String = user.email
    
    /**
     * 사용자 역할 확인
     */
    fun isTeacher(): Boolean = user.isTeacher()
    
    fun isStudent(): Boolean = user.isStudent()
    
    /**
     * 특정 권한 확인
     */
    fun hasAuthority(authority: String): Boolean = user.hasAuthority(authority)
} 