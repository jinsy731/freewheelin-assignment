package com.freewheelin.pulley.piece.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.freewheelin.pulley.common.infrastructure.security.SecurityService
import com.freewheelin.pulley.piece.application.port.*
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * PieceController 웹 계층 테스트
 */
@WebMvcTest(PieceController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
@ActiveProfiles("test")
class PieceControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @MockkBean
    private lateinit var pieceCreateUseCase: PieceCreateUseCase
    
    @MockkBean
    private lateinit var pieceOrderUpdateUseCase: PieceOrderUpdateUseCase
    
    @MockkBean
    private lateinit var pieceProblemsQueryUseCase: PieceProblemsQueryUseCase
    
    @MockkBean
    private lateinit var pieceAnalysisUseCase: PieceAnalysisUseCase
    
    @MockkBean
    private lateinit var securityService: SecurityService
    
    @Test
    fun `POST piece - 학습지 생성 성공`() {
        // Given
        val teacherId = 1L
        val requestDto = PieceCreateRequestDto(
            title = "수학 기초 학습지",
            problemIds = listOf(1L, 2L, 3L)
        )
        
        val result = PieceCreateResult(
            pieceId = 1L,
            name = "수학 기초 학습지"
        )
        
        every { securityService.requireCurrentUserId() } returns teacherId
        every { pieceCreateUseCase.createPiece(any()) } returns result
        
        // When & Then
        mockMvc.perform(
            post("/piece")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.pieceId").value(1))
            .andExpect(jsonPath("$.name").value("수학 기초 학습지"))
            .andExpect(jsonPath("$.message").value("학습지가 성공적으로 생성되었습니다"))
        
        verify { securityService.requireCurrentUserId() }
        verify { pieceCreateUseCase.createPiece(any()) }
    }
    
    @Test
    fun `POST piece - 잘못된 요청 데이터로 실패`() {
        // Given - 빈 제목과 빈 문제 ID 리스트
        val invalidRequestDto = PieceCreateRequestDto(
            title = "",
            problemIds = emptyList()
        )
        
        // When & Then
        mockMvc.perform(
            post("/piece")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestDto))
        )
            .andExpect(status().isBadRequest)
        
        verify(exactly = 0) { pieceCreateUseCase.createPiece(any()) }
    }
    
    @Test
    fun `PATCH piece pieceId order - 문제 순서 수정 성공`() {
        // Given
        val teacherId = 1L
        val pieceId = 1L
        val requestDto = ProblemOrderUpdateRequestDto(
            problemId = 3L,
            prevProblemId = 1L,
            nextProblemId = 2L
        )
        
        val result = ProblemOrderUpdateResult(
            pieceId = pieceId,
            problemId = 3L,
            previousPosition = 3.0,
            newPosition = 1.5,
            success = true
        )
        
        every { securityService.requireCurrentUserId() } returns teacherId
        every { pieceOrderUpdateUseCase.updateProblemOrder(any()) } returns result
        
        // When & Then
        mockMvc.perform(
            patch("/piece/{pieceId}/order", pieceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isOk)
        
        verify { securityService.requireCurrentUserId() }
        verify { pieceOrderUpdateUseCase.updateProblemOrder(any()) }
    }
    
    @Test
    fun `PATCH piece pieceId order - 잘못된 요청 데이터로 실패`() {
        // Given - 문제 ID가 없는 경우
        val pieceId = 1L
        val invalidRequestDto = mapOf(
            "prevProblemId" to 1L,
            "nextProblemId" to 2L
        )
        
        // When & Then
        mockMvc.perform(
            patch("/piece/{pieceId}/order", pieceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestDto))
        )
            .andExpect(status().isBadRequest)
        
        verify(exactly = 0) { pieceOrderUpdateUseCase.updateProblemOrder(any()) }
    }
    
    @Test
    fun `GET piece pieceId problems - 학습지 문제 조회 성공`() {
        // Given
        val userId = 1L
        val pieceId = 1L
        
        val result = PieceProblemsResult(
            problems = listOf(
                ProblemDetail(1L, "MATH-01", 3, ProblemType.SUBJECTIVE),
                ProblemDetail(2L, "MATH-02", 2, ProblemType.SUBJECTIVE),
                ProblemDetail(3L, "MATH-03", 4, ProblemType.SELECTION)
            )
        )
        
        every { securityService.requireCurrentUserId() } returns userId
        every { pieceProblemsQueryUseCase.getProblemsInPiece(any()) } returns result
        
        // When & Then
        mockMvc.perform(
            get("/piece/{pieceId}/problems", pieceId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.problems").isArray)
            .andExpect(jsonPath("$.problems.length()").value(3))
            .andExpect(jsonPath("$.problems[0].id").value(1))
            .andExpect(jsonPath("$.problems[0].unitCode").value("MATH-01"))
            .andExpect(jsonPath("$.problems[0].problemLevel").value(3))
            .andExpect(jsonPath("$.problems[0].type").value("SUBJECTIVE"))
        
        verify { securityService.requireCurrentUserId() }
        verify { pieceProblemsQueryUseCase.getProblemsInPiece(any()) }
    }
    
    @Test
    fun `GET piece pieceId analysis - 학습지 분석 성공`() {
        // Given
        val teacherId = 1L
        val pieceId = 1L
        
        val result = PieceAnalysisResult(
            pieceId = pieceId,
            pieceTitle = "수학 기초 학습지",
            assignedStudents = listOf(
                StudentStatistic(1L, "학생1", 0.8),
                StudentStatistic(2L, "학생2", 0.6)
            ),
            problemStats = listOf(
                ProblemStatistic(1L, 0.9),
                ProblemStatistic(2L, 0.7),
                ProblemStatistic(3L, 0.5)
            )
        )
        
        every { securityService.requireCurrentUserId() } returns teacherId
        every { pieceAnalysisUseCase.analyzePiece(any()) } returns result
        
        // When & Then
        mockMvc.perform(
            get("/piece/{pieceId}/analysis", pieceId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.pieceId").value(pieceId))
            .andExpect(jsonPath("$.pieceTitle").value("수학 기초 학습지"))
            .andExpect(jsonPath("$.assignedStudents").isArray)
            .andExpect(jsonPath("$.assignedStudents.length()").value(2))
            .andExpect(jsonPath("$.assignedStudents[0].studentName").value("학생1"))
            .andExpect(jsonPath("$.assignedStudents[0].correctnessRate").value(0.8))
            .andExpect(jsonPath("$.problemStats").isArray)
            .andExpect(jsonPath("$.problemStats.length()").value(3))
            .andExpect(jsonPath("$.problemStats[0].problemId").value(1))
            .andExpect(jsonPath("$.problemStats[0].correctnessRate").value(0.9))
        
        verify { securityService.requireCurrentUserId() }
        verify { pieceAnalysisUseCase.analyzePiece(any()) }
    }
}