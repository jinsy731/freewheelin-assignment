package com.freewheelin.pulley.common.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.freewheelin.pulley.user.domain.model.User
import com.freewheelin.pulley.user.domain.model.UserRole
import com.freewheelin.pulley.user.domain.port.UserRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * Spring Security 통합 테스트
 * 
 * 실제 Spring Security 설정을 사용하여 인증, 인가, 세션 관리를 종합적으로 테스트합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @MockkBean
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @BeforeEach
    fun setup() {
        // Mock user for authentication tests
        val teacherUser = User(
            id = 1L,
            username = "teacher1",
            password = "encoded_password",
            name = "선생님",
            email = "teacher1@example.com",
            role = UserRole.TEACHER,
            isActive = true,
            createdAt = LocalDateTime.now(),
        )

        val studentUser = User(
            id = 1L,
            username = "student1",
            password = "encoded_password",
            name = "학생",
            email = "student1@example.com",
            role = UserRole.STUDENT,
            isActive = true,
            createdAt = LocalDateTime.now(),
        )

        // Setup mock repository responses
        every { userRepository.findByUsername("teacher1") } returns Optional.of(teacherUser)
        every { userRepository.findByUsername("student1") } returns Optional.of(studentUser)
        every { userRepository.save(any()) } returnsArgument 0
    }

    
    @Test
    @WithMockCustomUser(username = "student1", roles = ["STUDENT"])
    fun `학생 권한 - 제한된 API만 접근 가능`() {
        // When & Then - 학습지 생성 시도 (선생님 전용 - 접근 금지)
        mockMvc.perform(
            post("/pieces")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "테스트학습지", "level": "ELEMENTARY", "problemCount": 5}""")
        )
            .andExpect(status().isForbidden)
    }
    
    @Test
    @WithMockCustomUser(username = "student1", roles = ["STUDENT"])
    fun `학생 권한 - 선생님 전용 API 접근 금지`() {
        // When & Then - 학습지 문제 순서 변경 (선생님 전용)
        mockMvc.perform(
            put("/pieces/1/problems/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"problemId": 1, "prevProblemId": null, "nextProblemId": 2}""")
        )
            .andExpect(status().isForbidden)
        
        // When & Then - 학습지 출제 (선생님 전용)
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"pieceId": 1, "studentIds": [1, 2]}""")
        )
            .andExpect(status().isForbidden)
        
        // When & Then - 학습지 분석 조회 (선생님 전용)
        mockMvc.perform(get("/pieces/1/analysis"))
            .andExpect(status().isForbidden)
    }

    
    @Test
    @WithMockCustomUser(username = "student1", roles = ["STUDENT"])
    fun `메소드 레벨 보안 - 권한 부족으로 접근 거부`() {
        // When & Then - CREATE_PIECE 권한이 없는 사용자의 접근 시도
        mockMvc.perform(
            post("/pieces")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "테스트학습지", "level": "ELEMENTARY", "problemCount": 5}""")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockCustomUser(username = "student1", roles = ["STUDENT"])
    fun `에러 처리 - 인가 실패 시 403 Forbidden`() {
        // When & Then - 권한이 없는 API에 접근
        mockMvc.perform(
            post("/pieces")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "테스트", "level": "ELEMENTARY", "problemCount": 5}""")
        )
            .andExpect(status().isForbidden)
    }
}