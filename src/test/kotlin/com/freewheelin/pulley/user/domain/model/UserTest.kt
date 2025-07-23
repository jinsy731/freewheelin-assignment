package com.freewheelin.pulley.user.domain.model

import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * User 도메인 엔티티 단위 테스트
 */
class UserTest {

    @Test
    fun `create - 정상적인 사용자 생성`() {
        // Given
        val username = "testuser"
        val password = "password123"
        val name = "테스트사용자"
        val email = "test@example.com"
        val role = UserRole.TEACHER

        // When
        val user = User.create(username, password, name, email, role)

        // Then
        assertEquals(username, user.username)
        assertEquals(password, user.password)
        assertEquals(name, user.name)
        assertEquals(email, user.email)
        assertEquals(role, user.role)
        assertTrue(user.isActive)
        assertNotNull(user.createdAt)
    }

    @Test
    fun `create - 사용자명 검증 실패 - 너무 짧음`() {
        // Given & When & Then
        assertThrows<ValidationException> {
            User.create("ab", "password123", "테스트", "test@example.com", UserRole.STUDENT)
        }
    }

    @Test
    fun `create - 사용자명 검증 실패 - 너무 김`() {
        // Given & When & Then
        assertThrows<ValidationException> {
            User.create("a".repeat(21), "password123", "테스트", "test@example.com", UserRole.STUDENT)
        }
    }

    @Test
    fun `create - 사용자명 검증 실패 - 공백`() {
        // Given & When & Then
        assertThrows<ValidationException> {
            User.create("", "password123", "테스트", "test@example.com", UserRole.STUDENT)
        }
    }

    @Test
    fun `create - 사용자명 검증 실패 - 잘못된 문자`() {
        // Given & When & Then
        assertThrows<ValidationException> {
            User.create("test@user", "password123", "테스트", "test@example.com", UserRole.STUDENT)
        }
    }

    @Test
    fun `create - 비밀번호 검증 실패 - 너무 짧음`() {
        // Given & When & Then
        assertThrows<ValidationException> {
            User.create("testuser", "1234567", "테스트", "test@example.com", UserRole.STUDENT)
        }
    }

    @Test
    fun `create - 이름 검증 실패 - 너무 짧음`() {
        // Given & When & Then
        assertThrows<ValidationException> {
            User.create("testuser", "password123", "a", "test@example.com", UserRole.STUDENT)
        }
    }

    @Test
    fun `create - 이름 검증 실패 - 너무 김`() {
        // Given & When & Then
        assertThrows<ValidationException> {
            User.create("testuser", "password123", "a".repeat(51), "test@example.com", UserRole.STUDENT)
        }
    }

    @Test
    fun `create - 이메일 검증 실패 - 잘못된 형식`() {
        // Given & When & Then
        assertThrows<ValidationException> {
            User.create("testuser", "password123", "테스트", "invalid-email", UserRole.STUDENT)
        }
    }

    @Test
    fun `hasAuthority - 활성 사용자의 권한 확인`() {
        // Given
        val user = User.create("teacher1", "password123", "선생님", "teacher@school.com", UserRole.TEACHER)

        // When & Then
        assertTrue(user.hasAuthority("ROLE_TEACHER"))
        assertTrue(user.hasAuthority("CREATE_PIECE"))
        assertFalse(user.hasAuthority("NON_EXISTENT_AUTHORITY"))
    }

    @Test
    fun `hasAuthority - 비활성 사용자의 권한 확인`() {
        // Given
        val user = User.create("teacher1", "password123", "선생님", "teacher@school.com", UserRole.TEACHER)
        val deactivatedUser = user.deactivate()

        // When & Then
        assertFalse(deactivatedUser.hasAuthority("ROLE_TEACHER"))
        assertFalse(deactivatedUser.hasAuthority("CREATE_PIECE"))
    }

    @Test
    fun `isTeacher - 선생님 역할 확인`() {
        // Given
        val teacher = User.create("teacher1", "password123", "선생님", "teacher@school.com", UserRole.TEACHER)
        val student = User.create("student1", "password123", "학생", "student@school.com", UserRole.STUDENT)

        // When & Then
        assertTrue(teacher.isTeacher())
        assertFalse(student.isTeacher())
    }

    @Test
    fun `isStudent - 학생 역할 확인`() {
        // Given
        val teacher = User.create("teacher1", "password123", "선생님", "teacher@school.com", UserRole.TEACHER)
        val student = User.create("student1", "password123", "학생", "student@school.com", UserRole.STUDENT)

        // When & Then
        assertFalse(teacher.isStudent())
        assertTrue(student.isStudent())
    }

    @Test
    fun `validateOwnership - 정상적인 소유권 확인`() {
        // Given
        val user = User.restore(1L, "testuser", "password", "테스트", "test@example.com", UserRole.TEACHER, true, LocalDateTime.now(), null)

        // When & Then (예외가 발생하지 않아야 함)
        user.validateOwnership(1L, "Piece", 100L)
    }

    @Test
    fun `validateOwnership - 비활성 사용자 예외`() {
        // Given
        val user = User.restore(1L, "testuser", "password", "테스트", "test@example.com", UserRole.TEACHER, false, LocalDateTime.now(), null)

        // When & Then
        assertThrows<AuthorizationException> {
            user.validateOwnership(1L, "Piece", 100L)
        }
    }

    @Test
    fun `validateOwnership - 소유자 불일치 예외`() {
        // Given
        val user = User.restore(1L, "testuser", "password", "테스트", "test@example.com", UserRole.TEACHER, true, LocalDateTime.now(), null)

        // When & Then
        assertThrows<AuthorizationException> {
            user.validateOwnership(2L, "Piece", 100L)  // 다른 사용자 ID
        }
    }

    @Test
    fun `updateLastLogin - 로그인 시간 업데이트`() {
        // Given
        val user = User.create("testuser", "password123", "테스트", "test@example.com", UserRole.TEACHER)

        // When
        val updatedUser = user.updateLastLogin()

        // Then
        assertNotNull(updatedUser.lastLoginAt)
        assertEquals(user.username, updatedUser.username)
        assertEquals(user.role, updatedUser.role)
    }

    @Test
    fun `deactivate - 사용자 비활성화`() {
        // Given
        val user = User.create("testuser", "password123", "테스트", "test@example.com", UserRole.TEACHER)

        // When
        val deactivatedUser = user.deactivate()

        // Then
        assertFalse(deactivatedUser.isActive)
        assertEquals(user.username, deactivatedUser.username)
        assertEquals(user.role, deactivatedUser.role)
    }

    @Test
    fun `equals - ID 기반 동등성 확인`() {
        // Given
        val user1 = User.restore(1L, "user1", "password", "사용자1", "user1@example.com", UserRole.TEACHER, true, LocalDateTime.now(), null)
        val user2 = User.restore(1L, "user2", "password", "사용자2", "user2@example.com", UserRole.STUDENT, true, LocalDateTime.now(), null)
        val user3 = User.restore(2L, "user1", "password", "사용자1", "user1@example.com", UserRole.TEACHER, true, LocalDateTime.now(), null)

        // When & Then
        assertEquals(user1, user2)  // 같은 ID
        assertTrue(user1 != user3)  // 다른 ID
    }

    @Test
    fun `hashCode - ID 기반 해시코드 확인`() {
        // Given
        val user1 = User.restore(1L, "user1", "password", "사용자1", "user1@example.com", UserRole.TEACHER, true, LocalDateTime.now(), null)
        val user2 = User.restore(1L, "user2", "password", "사용자2", "user2@example.com", UserRole.STUDENT, true, LocalDateTime.now(), null)

        // When & Then
        assertEquals(user1.hashCode(), user2.hashCode())  // 같은 ID는 같은 해시코드
    }
}