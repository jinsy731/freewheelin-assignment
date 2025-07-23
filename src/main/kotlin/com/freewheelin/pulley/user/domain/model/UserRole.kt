package com.freewheelin.pulley.user.domain.model

/**
 * 사용자 역할 정의
 * 
 * TEACHER: 선생님 - 모든 기능에 접근 가능
 * STUDENT: 학생 - 제한된 기능에만 접근 가능
 */
enum class UserRole(
    val description: String,
    val authorities: Set<String>
) {
    TEACHER(
        description = "선생님",
        authorities = setOf(
            "ROLE_TEACHER",
            "ROLE_STUDENT", // 선생님은 학생 기능도 모두 사용 가능
            "READ_PROBLEM",
            "CREATE_PIECE",
            "UPDATE_PIECE",
            "ASSIGN_PIECE",
            "GRADE_SUBMISSION",
            "VIEW_STATISTICS"
        )
    ),
    
    STUDENT(
        description = "학생",
        authorities = setOf(
            "ROLE_STUDENT",
            "READ_PROBLEM", // 정답 제외
            "VIEW_ASSIGNMENT",
            "SUBMIT_ANSWER"
        )
    );
    
    /**
     * 특정 권한을 가지고 있는지 확인
     */
    fun hasAuthority(authority: String): Boolean {
        return authorities.contains(authority)
    }
    
    /**
     * Spring Security 권한 문자열 목록 반환
     */
    fun getSecurityAuthorities(): Set<String> {
        return authorities
    }
} 