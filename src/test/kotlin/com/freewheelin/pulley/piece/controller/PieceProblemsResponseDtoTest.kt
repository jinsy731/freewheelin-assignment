package com.freewheelin.pulley.piece.controller

import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PieceProblemsResponseDtoTest {
    
    @Test
    fun `fromProblems - 정상 변환 테스트`() {
        // Given
        val pieceId = 100L
        val problems = listOf(
            Problem(1001L, "10", "A", 1, ProblemType.SELECTION),
            Problem(1002L, "20", "A", 2, ProblemType.SELECTION),
            Problem(1003L, "30", "B", 1, ProblemType.SUBJECTIVE)
        )
        
        // When
        val response = PieceProblemsDetailResponseDto.fromProblems(pieceId, problems)
        
        // Then
        assertEquals(100L, response.pieceId)
        assertEquals(3, response.totalCount)
        assertEquals(3, response.problems.size)
        
        // 첫 번째 문제 확인
        val firstProblem = response.problems[0]
        assertEquals(1001L, firstProblem.problemId)
        assertEquals(1, firstProblem.questionNumber)
        assertEquals("A", firstProblem.unitCode)
        assertEquals(1, firstProblem.level)
        assertEquals(ProblemType.SELECTION, firstProblem.problemType)
        
        // 두 번째 문제 확인
        val secondProblem = response.problems[1]
        assertEquals(1002L, secondProblem.problemId)
        assertEquals(2, secondProblem.questionNumber)
        assertEquals("A", secondProblem.unitCode)
        assertEquals(2, secondProblem.level)
        assertEquals(ProblemType.SELECTION, secondProblem.problemType)
        
        // 세 번째 문제 확인
        val thirdProblem = response.problems[2]
        assertEquals(1003L, thirdProblem.problemId)
        assertEquals(3, thirdProblem.questionNumber)
        assertEquals("B", thirdProblem.unitCode)
        assertEquals(1, thirdProblem.level)
        assertEquals(ProblemType.SUBJECTIVE, thirdProblem.problemType)
    }
    
    @Test
    fun `fromProblems - 단일 문제 변환 테스트`() {
        // Given
        val pieceId = 100L
        val problems = listOf(
            Problem(1001L, "10", "A", 1, ProblemType.SELECTION)
        )
        
        // When
        val response = PieceProblemsDetailResponseDto.fromProblems(pieceId, problems)
        
        // Then
        assertEquals(100L, response.pieceId)
        assertEquals(1, response.totalCount)
        assertEquals(1, response.problems.size)
        
        val problem = response.problems[0]
        assertEquals(1001L, problem.problemId)
        assertEquals(1, problem.questionNumber)
        assertEquals("A", problem.unitCode)
        assertEquals(1, problem.level)
        assertEquals(ProblemType.SELECTION, problem.problemType)
    }
    
    @Test
    fun `fromProblems - 빈 문제 리스트 변환 테스트`() {
        // Given
        val pieceId = 100L
        val problems = emptyList<Problem>()
        
        // When
        val response = PieceProblemsDetailResponseDto.fromProblems(pieceId, problems)
        
        // Then
        assertEquals(100L, response.pieceId)
        assertEquals(0, response.totalCount)
        assertEquals(0, response.problems.size)
    }
    
    @Test
    fun `PieceProblemDto fromDomain - 정상 변환 테스트`() {
        // Given
        val problem = Problem(1001L, "정답", "A", 3, ProblemType.SUBJECTIVE)
        val questionNumber = 5
        
        // When
        val dto = PieceProblemDto.fromDomain(problem, questionNumber)
        
        // Then
        assertEquals(1001L, dto.problemId)
        assertEquals(5, dto.questionNumber)
        assertEquals("A", dto.unitCode)
        assertEquals(3, dto.level)
        assertEquals(ProblemType.SUBJECTIVE, dto.problemType)
    }
    
    @Test
    fun `fromProblems - 문제 번호 순차 증가 확인`() {
        // Given
        val pieceId = 100L
        val problems = listOf(
            Problem(1001L, "답1", "A", 1, ProblemType.SELECTION),
            Problem(1002L, "답2", "B", 2, ProblemType.SUBJECTIVE),
            Problem(1003L, "답3", "C", 3, ProblemType.SELECTION),
            Problem(1004L, "답4", "D", 4, ProblemType.SUBJECTIVE),
            Problem(1005L, "답5", "E", 5, ProblemType.SELECTION)
        )
        
        // When
        val response = PieceProblemsDetailResponseDto.fromProblems(pieceId, problems)
        
        // Then
        assertEquals(5, response.problems.size)
        
        // 문제 번호가 1부터 순차적으로 증가하는지 확인
        response.problems.forEachIndexed { index, problem ->
            assertEquals(index + 1, problem.questionNumber)
        }
        
        // 각 문제의 ID와 정보가 순서대로 매핑되었는지 확인
        assertEquals(1001L, response.problems[0].problemId)
        assertEquals(1002L, response.problems[1].problemId)
        assertEquals(1003L, response.problems[2].problemId)
        assertEquals(1004L, response.problems[3].problemId)
        assertEquals(1005L, response.problems[4].problemId)
    }
} 