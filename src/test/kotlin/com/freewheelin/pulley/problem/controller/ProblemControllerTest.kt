package com.freewheelin.pulley.problem.controller

import com.freewheelin.pulley.common.domain.Level
import com.freewheelin.pulley.problem.application.port.ProblemSearchQuery
import com.freewheelin.pulley.problem.application.port.ProblemSearchResult
import com.freewheelin.pulley.problem.application.port.ProblemSearchUseCase
import com.freewheelin.pulley.problem.application.port.ProblemTypeFilter
import com.freewheelin.pulley.problem.application.port.ProblemInfo
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.common.infrastructure.security.SecurityService
import com.freewheelin.pulley.common.presentation.GlobalExceptionHandler
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * ProblemController 단위 테스트
 * 
 * @WebMvcTest를 사용하여 웹 계층만 테스트합니다.
 */
@WebMvcTest(ProblemController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
@Import(GlobalExceptionHandler::class)
class ProblemControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockkBean
    private lateinit var problemSearchUseCase: ProblemSearchUseCase
    
    @MockkBean
    private lateinit var securityService: SecurityService

    @BeforeEach
    fun setUp() {
        every { securityService.isCurrentUserTeacher() } returns true
    }
    
    @Test
    fun `정상적인 문제 조회 요청 - 200 OK 응답`() {
        // Given
        val expectedResult = ProblemSearchResult(
            problems = listOf(
                ProblemInfo(
                    id = 1L,
                    answer = "답1",
                    unitCode = "UC001",
                    level = 1,
                    problemType = ProblemType.SUBJECTIVE
                ),
                ProblemInfo(
                    id = 2L,
                    answer = "답2",
                    unitCode = "UC001",
                    level = 2,
                    problemType = ProblemType.SELECTION
                )
            ),
            totalCount = 2
        )
        
        every { problemSearchUseCase.searchProblems(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                .param("unitCodeList", "UC001,UC002")
                .param("level", "MIDDLE")
                .param("problemType", "ALL")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.problemList").isArray)
            .andExpect(jsonPath("$.problemList.length()").value(2))
            .andExpect(jsonPath("$.totalCount").value(2))
            .andExpect(jsonPath("$.problemList[0].id").value(1))
            .andExpect(jsonPath("$.problemList[0].answer").value("답1"))
            .andExpect(jsonPath("$.problemList[0].unitCode").value("UC001"))
            .andExpect(jsonPath("$.problemList[0].problemLevel").value(1))
            .andExpect(jsonPath("$.problemList[0].problemType").value("SUBJECTIVE"))

        verify(exactly = 1) {
            problemSearchUseCase.searchProblems(match<ProblemSearchQuery> {
                it.totalCount == 5 &&
                        it.unitCodeList == listOf("UC001", "UC002") &&
                        it.level == Level.MIDDLE &&
                        it.problemType == ProblemTypeFilter.ALL
            })
        }
    }
    
    @Test
    fun `주관식 문제만 조회`() {
        // Given
        val expectedResult = ProblemSearchResult(
            problems = listOf(
                ProblemInfo(
                    id = 1L,
                    answer = "답1",
                    unitCode = "UC001",
                    level = 3,
                    problemType = ProblemType.SUBJECTIVE
                )
            ),
            totalCount = 1
        )
        
        every { problemSearchUseCase.searchProblems(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "3")
                .param("unitCodeList", "UC001")
                .param("level", "HIGH")
                .param("problemType", "SUBJECTIVE")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.problemList.length()").value(1))
            .andExpect(jsonPath("$.problemList[0].problemType").value("SUBJECTIVE"))

        verify(exactly = 1) {
            problemSearchUseCase.searchProblems(match<ProblemSearchQuery> {
                it.problemType == ProblemTypeFilter.SUBJECTIVE
            })
        }
    }
    
    @Test
    fun `객관식 문제만 조회`() {
        // Given
        val expectedResult = ProblemSearchResult(
            problems = listOf(
                ProblemInfo(
                    id = 2L,
                    answer = "답2",
                    unitCode = "UC002",
                    level = 2,
                    problemType = ProblemType.SELECTION
                )
            ),
            totalCount = 1
        )
        
        every { problemSearchUseCase.searchProblems(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "3")
                .param("unitCodeList", "UC002")
                .param("level", "LOW")
                .param("problemType", "SELECTION")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.problemList[0].problemType").value("SELECTION"))

        verify(exactly = 1) {
            problemSearchUseCase.searchProblems(match<ProblemSearchQuery> {
                it.problemType == ProblemTypeFilter.SELECTION &&
                        it.level == Level.LOW
            })
        }
    }
    
    @Test
    fun `여러 유형코드로 조회 - 쉼표로 구분된 문자열 파싱`() {
        // Given
        val expectedResult = ProblemSearchResult(
            problems = emptyList(),
            totalCount = 0
        )
        
        every { problemSearchUseCase.searchProblems(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "10")
                .param("unitCodeList", "UC001, UC002 , UC003") // 공백 포함
                .param("level", "MIDDLE")
                .param("problemType", "ALL")
        )
            .andExpect(status().isOk)

        verify(exactly = 1) {
            problemSearchUseCase.searchProblems(match<ProblemSearchQuery> {
                it.unitCodeList == listOf("UC001", "UC002", "UC003")
            })
        }
    }
    
    @Test
    fun `빈 결과 조회 - 조건에 맞는 문제가 없는 경우`() {
        // Given
        val expectedResult = ProblemSearchResult(
            problems = emptyList(),
            totalCount = 0
        )
        
        every { problemSearchUseCase.searchProblems(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                .param("unitCodeList", "UC999") // 존재하지 않는 유형코드
                .param("level", "HIGH")
                .param("problemType", "ALL")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.problemList").isArray)
            .andExpect(jsonPath("$.problemList.length()").value(0))
            .andExpect(jsonPath("$.totalCount").value(0))
    }
    
    @Test
    fun `잘못된 요청 - totalCount가 누락된 경우 400 Bad Request`() {
        // When & Then
        mockMvc.perform(
            get("/problems")
                // totalCount 누락
                .param("unitCodeList", "UC001")
                .param("level", "MIDDLE")
                .param("problemType", "ALL")
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `잘못된 요청 - totalCount가 0인 경우 400 Bad Request`() {
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "0") // 0은 허용되지 않음
                .param("unitCodeList", "UC001")
                .param("level", "MIDDLE")
                .param("problemType", "ALL")
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `잘못된 요청 - unitCodeList가 누락된 경우 400 Bad Request`() {
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                // unitCodeList 누락
                .param("level", "MIDDLE")
                .param("problemType", "ALL")
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `잘못된 요청 - level이 잘못된 값인 경우 400 Bad Request`() {
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                .param("unitCodeList", "UC001")
                .param("level", "INVALID_LEVEL") // 잘못된 Level 값
                .param("problemType", "ALL")
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `잘못된 요청 - problemType이 잘못된 값인 경우 400 Bad Request`() {
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                .param("unitCodeList", "UC001")
                .param("level", "MIDDLE")
                .param("problemType", "INVALID_TYPE") // 잘못된 ProblemTypeFilter 값
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `모든 Level 값으로 조회 테스트`() {
        // Given
        val expectedResult = ProblemSearchResult(problems = emptyList(), totalCount = 0)
        every { problemSearchUseCase.searchProblems(any()) } returns expectedResult
        
        // LOW 레벨 테스트
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                .param("unitCodeList", "UC001")
                .param("level", "LOW")
                .param("problemType", "ALL")
        )
            .andExpect(status().isOk)
        
        // MIDDLE 레벨 테스트
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                .param("unitCodeList", "UC001")
                .param("level", "MIDDLE")
                .param("problemType", "ALL")
        )
            .andExpect(status().isOk)
        
        // HIGH 레벨 테스트
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                .param("unitCodeList", "UC001")
                .param("level", "HIGH")
                .param("problemType", "ALL")
        )
            .andExpect(status().isOk)

        verify(exactly = 3) { problemSearchUseCase.searchProblems(any()) }
    }
    
    @Test
    fun `unitCodeList에 빈 값이 포함된 경우 - 필터링되어 처리`() {
        // Given
        val expectedResult = ProblemSearchResult(problems = emptyList(), totalCount = 0)
        every { problemSearchUseCase.searchProblems(any()) } returns expectedResult
        
        // When & Then
        mockMvc.perform(
            get("/problems")
                .param("totalCount", "5")
                .param("unitCodeList", "UC001,,UC002, ,UC003") // 빈 값들 포함
                .param("level", "MIDDLE")
                .param("problemType", "ALL")
        )
            .andExpect(status().isOk)

        verify(exactly = 1) {
            problemSearchUseCase.searchProblems(match<ProblemSearchQuery> {
                it.unitCodeList == listOf("UC001", "UC002", "UC003") // 빈 값들이 필터링됨
            })
        }
    }
} 