package com.freewheelin.pulley.assignment.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.freewheelin.pulley.assignment.application.port.AssignmentCreateRequest
import com.freewheelin.pulley.assignment.application.port.AssignmentCreateResult
import com.freewheelin.pulley.assignment.application.port.AssignmentCreateUseCase
import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.common.infrastructure.security.SecurityService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.text.trimIndent

/**
 * AssignmentController 단위 테스트
 * 
 * @WebMvcTest를 사용하여 웹 계층만 테스트합니다.
 */
@WebMvcTest(AssignmentController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
class AssignmentControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @MockkBean
    private lateinit var assignmentCreateUseCase: AssignmentCreateUseCase
    
    @MockkBean
    private lateinit var securityService: SecurityService
    
    @Test
    fun `정상적인 출제 요청 - 201 Created 응답`() {
        // Given
        every { securityService.requireCurrentUserId() } returns 1L
        
        val requestDto = AssignmentCreateRequestDto(
            pieceId = 2001L,
            studentIds = listOf(101L, 102L, 103L)
        )
        
        val expectedResult = AssignmentCreateResult(
            pieceId = 2001L,
            totalRequestedStudents = 3,
            newlyAssignedStudents = 3,
            alreadyAssignedStudents = 0,
            newlyAssignedStudentIds = listOf(101L, 102L, 103L),
            alreadyAssignedStudentIds = emptyList()
        )
        
        every { assignmentCreateUseCase.assignPiece(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.pieceId").value(2001))
            .andExpect(jsonPath("$.totalRequestedStudents").value(3))
            .andExpect(jsonPath("$.newlyAssignedStudents").value(3))
            .andExpect(jsonPath("$.alreadyAssignedStudents").value(0))
            .andExpect(jsonPath("$.newlyAssignedStudentIds").isArray)
            .andExpect(jsonPath("$.newlyAssignedStudentIds.length()").value(3))
            .andExpect(jsonPath("$.alreadyAssignedStudentIds").isArray)
            .andExpect(jsonPath("$.alreadyAssignedStudentIds.length()").value(0))
            .andExpect(jsonPath("$.isAllStudentsProcessed").value(true))

        verify(exactly = 1) {
            assignmentCreateUseCase.assignPiece(match<AssignmentCreateRequest> {
                it.teacherId == 1L &&
                        it.pieceId == 2001L &&
                        it.studentIds == listOf(101L, 102L, 103L)
            })
        }
    }
    
    @Test
    fun `부분 출제 - 일부 학생은 이미 출제된 경우`() {
        // Given
        every { securityService.requireCurrentUserId() } returns 1L
        
        val requestDto = AssignmentCreateRequestDto(
            pieceId = 2001L,
            studentIds = listOf(101L, 102L, 103L, 104L)
        )
        
        val expectedResult = AssignmentCreateResult(
            pieceId = 2001L,
            totalRequestedStudents = 4,
            newlyAssignedStudents = 2,
            alreadyAssignedStudents = 2,
            newlyAssignedStudentIds = listOf(103L, 104L),
            alreadyAssignedStudentIds = listOf(101L, 102L)
        )
        
        every { assignmentCreateUseCase.assignPiece(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.pieceId").value(2001))
            .andExpect(jsonPath("$.totalRequestedStudents").value(4))
            .andExpect(jsonPath("$.newlyAssignedStudents").value(2))
            .andExpect(jsonPath("$.alreadyAssignedStudents").value(2))
            .andExpect(jsonPath("$.newlyAssignedStudentIds.length()").value(2))
            .andExpect(jsonPath("$.alreadyAssignedStudentIds.length()").value(2))
            .andExpect(jsonPath("$.isAllStudentsProcessed").value(true))
    }
    
    @Test
    fun `잘못된 요청 - teacherId가 null인 경우 400 Bad Request`() {
        // Given
        val invalidRequest = """
            {
                "pieceId": 2001,
                "studentIds": [101, 102, 103]
            }
        """.trimIndent()
        every { securityService.requireCurrentUserId() } throws kotlin.IllegalStateException("인증된 사용자 ID를 찾을 수 없습니다.")


        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isConflict)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorCode").value("E006"))
            .andExpect(jsonPath("$.message").value("인증된 사용자 ID를 찾을 수 없습니다."))
            .andExpect(jsonPath("$.details").isArray)
    }
    
    @Test
    fun `잘못된 요청 - pieceId가 음수인 경우 400 Bad Request`() {
        // Given
        val requestDto = AssignmentCreateRequestDto(
            pieceId = -1L, // 음수
            studentIds = listOf(101L, 102L)
        )
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errorCode").value("E005"))
            .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다"))
    }
    
    @Test
    fun `잘못된 요청 - studentIds가 빈 리스트인 경우 400 Bad Request`() {
        // Given
        val requestDto = AssignmentCreateRequestDto(
            pieceId = 2001L,
            studentIds = emptyList() // 빈 리스트
        )
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errorCode").value("E005"))
    }
    
    @Test
    fun `잘못된 요청 - studentIds에 음수가 포함된 경우 400 Bad Request`() {
        // Given
        val requestDto = AssignmentCreateRequestDto(
            pieceId = 2001L,
            studentIds = listOf(101L, -1L, 103L) // 음수 포함
        )
        every { securityService.requireCurrentUserId() } returns 1L

        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errorCode").value("E004")) // IllegalArgumentException으로 처리됨
    }
    
    @Test
    fun `권한 없음 - 다른 선생님의 학습지에 출제 시도 시 403 Forbidden`() {
        // Given
        every { securityService.requireCurrentUserId() } returns 1L
        
        val requestDto = AssignmentCreateRequestDto(
            pieceId = 2001L,
            studentIds = listOf(101L, 102L)
        )
        
        every { assignmentCreateUseCase.assignPiece(any()) } throws AuthorizationException(
            ErrorCode.PIECE_UNAUTHORIZED,
            1L,
            "Piece",
            2001L
        )
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isForbidden)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorCode").value("P002"))
    }
    
    @Test
    fun `리소스 없음 - 존재하지 않는 학습지에 출제 시도 시 404 Not Found`() {
        // Given
        every { securityService.requireCurrentUserId() } returns 1L
        
        val requestDto = AssignmentCreateRequestDto(
            pieceId = 999L, // 존재하지 않는 학습지
            studentIds = listOf(101L, 102L)
        )
        
        every { assignmentCreateUseCase.assignPiece(any()) } throws NotFoundException(
            ErrorCode.PIECE_NOT_FOUND,
            999L
        )
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorCode").value("P001"))
    }
    
    @Test
    fun `단일 학생 출제`() {
        // Given
        every { securityService.requireCurrentUserId() } returns 1L
        
        val requestDto = AssignmentCreateRequestDto(
            pieceId = 2001L,
            studentIds = listOf(101L)
        )
        
        val expectedResult = AssignmentCreateResult(
            pieceId = 2001L,
            totalRequestedStudents = 1,
            newlyAssignedStudents = 1,
            alreadyAssignedStudents = 0,
            newlyAssignedStudentIds = listOf(101L),
            alreadyAssignedStudentIds = emptyList()
        )
        
        every { assignmentCreateUseCase.assignPiece(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.pieceId").value(2001))
            .andExpect(jsonPath("$.newlyAssignedStudents").value(1))
            .andExpect(jsonPath("$.newlyAssignedStudentIds[0]").value(101))
    }
    
    @Test
    fun `잘못된 JSON 형식 - 400 Bad Request`() {
        // Given
        val invalidJson = """
            {
                "teacherId": 1,
                "pieceId": 2001,
                "studentIds": [101, 102, // 잘못된 JSON
        """.trimIndent()
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `Content-Type이 없는 경우 - 415 Unsupported Media Type`() {
        // Given
        val requestDto = AssignmentCreateRequestDto(
            pieceId = 2001L,
            studentIds = listOf(101L, 102L)
        )
        
        // When & Then
        mockMvc.perform(
            post("/assignments")
                // Content-Type 헤더 없음
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isUnsupportedMediaType)
    }
} 