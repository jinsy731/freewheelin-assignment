package com.freewheelin.pulley.user.domain.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * UserRole enum 단위 테스트
 */
class UserRoleTest {
    
    @Test
    fun `TEACHER - 권한 목록 확인`() {
        // Given
        val role = UserRole.TEACHER
        
        // When & Then
        assertEquals("선생님", role.description)
        assertTrue(role.authorities.contains("ROLE_TEACHER"))
        assertTrue(role.authorities.contains("ROLE_STUDENT"))  // 선생님은 학생 기능도 사용 가능
        assertTrue(role.authorities.contains("READ_PROBLEM"))
        assertTrue(role.authorities.contains("CREATE_PIECE"))
        assertTrue(role.authorities.contains("UPDATE_PIECE"))
        assertTrue(role.authorities.contains("ASSIGN_PIECE"))
        assertTrue(role.authorities.contains("GRADE_SUBMISSION"))
        assertTrue(role.authorities.contains("VIEW_STATISTICS"))
    }
    
    @Test
    fun `STUDENT - 권한 목록 확인`() {
        // Given
        val role = UserRole.STUDENT
        
        // When & Then
        assertEquals("학생", role.description)
        assertTrue(role.authorities.contains("ROLE_STUDENT"))
        assertTrue(role.authorities.contains("READ_PROBLEM"))
        assertTrue(role.authorities.contains("VIEW_ASSIGNMENT"))
        assertTrue(role.authorities.contains("SUBMIT_ANSWER"))
        
        // 선생님 전용 권한은 없어야 함
        assertFalse(role.authorities.contains("ROLE_TEACHER"))
        assertFalse(role.authorities.contains("CREATE_PIECE"))
        assertFalse(role.authorities.contains("UPDATE_PIECE"))
        assertFalse(role.authorities.contains("ASSIGN_PIECE"))
        assertFalse(role.authorities.contains("GRADE_SUBMISSION"))
        assertFalse(role.authorities.contains("VIEW_STATISTICS"))
    }
    
    @Test
    fun `hasAuthority - TEACHER 권한 확인`() {
        // Given
        val role = UserRole.TEACHER
        
        // When & Then
        assertTrue(role.hasAuthority("ROLE_TEACHER"))
        assertTrue(role.hasAuthority("ROLE_STUDENT"))
        assertTrue(role.hasAuthority("CREATE_PIECE"))
        assertFalse(role.hasAuthority("NON_EXISTENT_AUTHORITY"))
    }
    
    @Test
    fun `hasAuthority - STUDENT 권한 확인`() {
        // Given
        val role = UserRole.STUDENT
        
        // When & Then
        assertTrue(role.hasAuthority("ROLE_STUDENT"))
        assertTrue(role.hasAuthority("READ_PROBLEM"))
        assertFalse(role.hasAuthority("ROLE_TEACHER"))
        assertFalse(role.hasAuthority("CREATE_PIECE"))
        assertFalse(role.hasAuthority("NON_EXISTENT_AUTHORITY"))
    }
    
    @Test
    fun `getSecurityAuthorities - TEACHER 권한 반환`() {
        // Given
        val role = UserRole.TEACHER
        
        // When
        val authorities = role.getSecurityAuthorities()
        
        // Then
        assertTrue(authorities.contains("ROLE_TEACHER"))
        assertTrue(authorities.contains("ROLE_STUDENT"))
        assertTrue(authorities.contains("CREATE_PIECE"))
        assertEquals(8, authorities.size)  // 모든 권한 수
    }
    
    @Test
    fun `getSecurityAuthorities - STUDENT 권한 반환`() {
        // Given
        val role = UserRole.STUDENT
        
        // When
        val authorities = role.getSecurityAuthorities()
        
        // Then
        assertTrue(authorities.contains("ROLE_STUDENT"))
        assertTrue(authorities.contains("READ_PROBLEM"))
        assertFalse(authorities.contains("ROLE_TEACHER"))
        assertEquals(4, authorities.size)  // 학생 권한 수
    }
} 