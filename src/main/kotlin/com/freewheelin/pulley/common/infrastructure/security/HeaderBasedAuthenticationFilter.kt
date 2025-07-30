package com.freewheelin.pulley.common.infrastructure.security

import com.freewheelin.pulley.user.domain.model.User
import com.freewheelin.pulley.user.domain.port.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 헤더 기반 인증 필터
 * 
 * X-User-Id 헤더를 통해 사용자를 식별하고 SecurityContext에 인증 정보를 설정합니다.
 * 데모 목적으로 간소화된 인증 방식입니다.
 */
class HeaderBasedAuthenticationFilter(
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    private val logger = KotlinLogging.logger {}

    companion object {
        const val USER_ID_HEADER = "X-User-Id"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userId = request.getHeader(USER_ID_HEADER)
        
        // 헤더가 없으면 인증되지 않은 상태로 계속 진행
        if (userId.isNullOrBlank()) {
            logger.debug { "X-User-Id 헤더가 없습니다. 인증되지 않은 상태로 진행합니다." }
            filterChain.doFilter(request, response)
            return
        }
        
        // runCatching을 사용하여 사용자 인증 처리
        runCatching {
            // 사용자 ID 파싱
            val userIdLong = runCatching { 
                userId.toLong() 
            }.getOrElse { e ->
                logger.warn { "잘못된 형식의 사용자 ID - value: $userId" }
                filterChain.doFilter(request, response)
                return@runCatching
            }
            
            // 사용자 조회 및 검증
            val user = userRepository.findById(userIdLong).orElse(null)
                ?: run {
                    logger.warn { "존재하지 않는 사용자 ID로 접근 시도 - userId: $userId" }
                    filterChain.doFilter(request, response)
                    return@runCatching
                }
            
            // 사용자 활성 상태 확인
            if (!user.isActive) {
                logger.warn { "비활성화된 사용자 접근 시도 - userId: ${user.id}" }
                filterChain.doFilter(request, response)
                return@runCatching
            }
            
            // 모든 검증을 통과한 경우에만 인증 정보 설정
            setAuthentication(user)
            logger.debug { "사용자 인증 성공 - userId: ${user.id}, username: ${user.username}, role: ${user.role}" }
        }.onFailure { e ->
            logger.error { "사용자 인증 중 오류 발생 - userId: $userId" }
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun setAuthentication(user: User) {
        val userPrincipal = CustomUserPrincipal(user)
        
        val authentication = UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.authorities
        )

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
        
        logger.debug { "사용자 인증 정보 설정 완료 - userId: ${user.id}, username: ${user.username}, authorities: ${userPrincipal.authorities}" }
    }
}