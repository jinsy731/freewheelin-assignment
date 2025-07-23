package com.freewheelin.pulley.piece.domain.service

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * PieceProblemFactory 단위 테스트
 */
class PieceProblemFactoryTest {
    
    @Test
    fun `createFromProblems - 문제들을 유형코드와 난이도 순으로 정렬하여 PieceProblem 생성`() {
        // Given
        val pieceId = PieceId(1L)
        val problems = listOf(
            createProblem(1L, "Unit2", 3),  // 3번째
            createProblem(2L, "Unit1", 5),  // 2번째 
            createProblem(3L, "Unit2", 1),  // 4번째
            createProblem(4L, "Unit1", 2)   // 1번째
        )
        
        // When
        val pieceProblems = PieceProblemFactory.createFromProblems(pieceId, problems)
        
        // Then
        assertEquals(4, pieceProblems.size)
        
        // 정렬 순서 확인: Unit1-2 -> Unit1-5 -> Unit2-1 -> Unit2-3
        assertEquals(ProblemId(4L), pieceProblems[0].problemId) // Unit1, level 2
        assertEquals(ProblemId(2L), pieceProblems[1].problemId) // Unit1, level 5
        assertEquals(ProblemId(3L), pieceProblems[2].problemId) // Unit2, level 1
        assertEquals(ProblemId(1L), pieceProblems[3].problemId) // Unit2, level 3
        
        // 위치 값 확인
        assertEquals(1.0, pieceProblems[0].position.value)
        assertEquals(2.0, pieceProblems[1].position.value)
        assertEquals(3.0, pieceProblems[2].position.value)
        assertEquals(4.0, pieceProblems[3].position.value)
        
        // PieceId 확인
        pieceProblems.forEach { pieceProblem ->
            assertEquals(pieceId, pieceProblem.pieceId)
        }
    }
    
    @Test
    fun `createFromProblems - 같은 유형코드 내에서 난이도 순 정렬`() {
        // Given
        val pieceId = PieceId(1L)
        val problems = listOf(
            createProblem(1L, "Unit1", 5),  // 3번째
            createProblem(2L, "Unit1", 1),  // 1번째
            createProblem(3L, "Unit1", 3)   // 2번째
        )
        
        // When
        val pieceProblems = PieceProblemFactory.createFromProblems(pieceId, problems)
        
        // Then
        assertEquals(3, pieceProblems.size)
        
        // 난이도 순서 확인: level 1 -> 3 -> 5
        assertEquals(ProblemId(2L), pieceProblems[0].problemId) // level 1
        assertEquals(ProblemId(3L), pieceProblems[1].problemId) // level 3
        assertEquals(ProblemId(1L), pieceProblems[2].problemId) // level 5
    }
    
    @Test
    fun `createFromProblems - 단일 문제로 생성`() {
        // Given
        val pieceId = PieceId(1L)
        val problems = listOf(createProblem(1L, "Unit1", 1))
        
        // When
        val pieceProblems = PieceProblemFactory.createFromProblems(pieceId, problems)
        
        // Then
        assertEquals(1, pieceProblems.size)
        assertEquals(ProblemId(1L), pieceProblems[0].problemId)
        assertEquals(1.0, pieceProblems[0].position.value)
        assertEquals(pieceId, pieceProblems[0].pieceId)
    }
    
    @Test
    fun `createFromProblems - 빈 문제 리스트로 호출시 예외 발생`() {
        // Given
        val pieceId = PieceId(1L)
        val emptyProblems = emptyList<Problem>()
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            PieceProblemFactory.createFromProblems(pieceId, emptyProblems)
        }
        
        assertEquals("문제 리스트는 비어있을 수 없습니다.", exception.message)
    }
    
    @Test
    fun `createFromProblems - 복잡한 정렬 시나리오`() {
        // Given
        val pieceId = PieceId(1L)
        val problems = listOf(
            createProblem(1L, "C", 2),   // 5번째
            createProblem(2L, "A", 3),   // 2번째
            createProblem(3L, "B", 1),   // 3번째  
            createProblem(4L, "A", 1),   // 1번째
            createProblem(5L, "B", 2)    // 4번째
        )
        
        // When
        val pieceProblems = PieceProblemFactory.createFromProblems(pieceId, problems)
        
        // Then
        assertEquals(5, pieceProblems.size)
        
        // 예상 순서: A-1, A-3, B-1, B-2, C-2
        assertEquals(ProblemId(4L), pieceProblems[0].problemId) // A-1
        assertEquals(ProblemId(2L), pieceProblems[1].problemId) // A-3
        assertEquals(ProblemId(3L), pieceProblems[2].problemId) // B-1
        assertEquals(ProblemId(5L), pieceProblems[3].problemId) // B-2
        assertEquals(ProblemId(1L), pieceProblems[4].problemId) // C-2
        
        // 위치 값이 순차적으로 증가하는지 확인
        (0 until 5).forEach { index ->
            assertEquals((index + 1).toDouble(), pieceProblems[index].position.value)
        }
    }
    
    private fun createProblem(id: Long, unitCode: String, level: Int): Problem {
        return Problem(
            id = id,
            answer = "정답",
            unitCode = unitCode,
            level = level,
            problemType = ProblemType.SUBJECTIVE
        )
    }
} 