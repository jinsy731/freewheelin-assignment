package com.freewheelin.pulley.common.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.presentation.ErrorResponse
import com.freewheelin.pulley.user.domain.port.UserRepository
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * Spring Security 설정
 * 
 * 헤더 기반 인증과 Role 기반 접근 제어를 구성합니다.
 * 데모 목적으로 간소화된 인증 방식입니다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository
) {
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    
    @Bean
    fun headerBasedAuthenticationFilter(): HeaderBasedAuthenticationFilter {
        return HeaderBasedAuthenticationFilter(userRepository)
    }
    
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(
                        "/actuator/health",   // 헬스체크
                        "/error",             // 에러 페이지
                        "/css/**",            // 정적 리소스
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        // Swagger UI 관련 경로
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll()

                    .requestMatchers(toH2Console()).permitAll()

                    // 선생님 전용 기능
                    .requestMatchers("POST", "/pieces").hasRole("TEACHER")
                    .requestMatchers("PUT", "/pieces/*/problems/order").hasRole("TEACHER") 
                    .requestMatchers("POST", "/assignments").hasRole("TEACHER")
                    .requestMatchers("GET", "/pieces/*/analysis").hasRole("TEACHER")
                    
                    // 공통 기능 (선생님과 학생 모두 접근 가능)
                    .requestMatchers("/problems/**").hasAnyRole("TEACHER", "STUDENT")
                    .requestMatchers("/pieces/*/problems").hasAnyRole("TEACHER", "STUDENT")
                    .requestMatchers("/pieces/*/score").hasAnyRole("TEACHER", "STUDENT")
                    
                    // 그 외 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            
            .formLogin { form -> form.disable() }
            
            .logout { logout -> logout.disable() }
            
            // 예외 처리 설정
            .exceptionHandling { exception ->
                exception
                    .authenticationEntryPoint { request, response, authException ->
                        val remoteAddr = request.remoteAddr
                        val requestUri = request.requestURI
                        logger.warn { 
                            "인증 실패 - 미인증 사용자의 보호된 리소스 접근 시도: " +
                            "URI=$requestUri, IP=$remoteAddr, error=${authException.message}" 
                        }
                        
                        // JSON 응답 반환
                        response.contentType = "application/json;charset=UTF-8"
                        response.status = 401
                        
                        val errorResponse = ErrorResponse(
                            errorCode = ErrorCode.AUTHENTICATION_FAILED.code,
                            message = "인증이 필요합니다. X-User-Id 헤더를 설정해주세요.",
                            details = emptyList(),
                            timestamp = LocalDateTime.now(),
                            path = requestUri
                        )
                        
                        objectMapper.writeValue(response.writer, errorResponse)
                    }
            }

            .headers { headers -> headers.frameOptions { it.disable() } }
            
            // 헤더 기반 인증 필터 추가
            .addFilterBefore(headerBasedAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
} 