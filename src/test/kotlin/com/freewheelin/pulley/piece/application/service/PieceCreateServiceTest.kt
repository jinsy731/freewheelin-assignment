package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.PieceName
import com.freewheelin.pulley.common.domain.TeacherId
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.PieceCreateRequest
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import com.freewheelin.pulley.piece.domain.service.PieceProblemFactory
import com.freewheelin.pulley.problem.domain.model.Problem
import com.freewheelin.pulley.problem.domain.model.ProblemType
import com.freewheelin.pulley.problem.domain.port.ProblemRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PieceCreateServiceTest {
    
    private val pieceRepository = mockk<PieceRepository>()
    private val pieceProblemRepository = mockk<PieceProblemRepository>()
    private val problemRepository = mockk<ProblemRepository>()
    private val pieceCreateService = PieceCreateService(
        pieceRepository,
        pieceProblemRepository,
        problemRepository
    )
    
    @Test
    fun `createPiece - 정상 생성 테스트`() {
        // Given
        val request = PieceCreateRequest(
            teacherId = 1L,
            title = "수학 학습지",
            problemIds = listOf(1L, 2L, 3L)
        )
        
        val mockProblems = listOf(
            Problem(1L, "답1", "A", 2, ProblemType.SELECTION),
            Problem(2L, "답2", "B", 1, ProblemType.SUBJECTIVE),  
            Problem(3L, "답3", "C", 3, ProblemType.SELECTION)
        )
        
        val savedPiece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),
            name = PieceName("수학 학습지")
        )
        
        
        val pieceProblems = PieceProblemFactory.createFromProblems(
            PieceId(100L),
                mockProblems)
        
        every { problemRepository.findByIds(request.problemIds) } returns mockProblems
        every { pieceRepository.save(any()) } returns savedPiece
        every { pieceProblemRepository.saveAll(any()) } returns emptyList()
        
        // When
        val result = pieceCreateService.createPiece(request)
        
        // Then
        assertEquals(100L, result.pieceId)
        assertEquals("수학 학습지", result.name)
        
        // Repository와 Factory 호출 검증
        verify { pieceRepository.save(any()) }
        verify { pieceProblemRepository.saveAll(pieceProblems) }
        
        // 문제 순서 검증
        assertEquals(1.0, pieceProblems[0].position.value, "첫 번째 문제의 위치는 1.0이어야 합니다")
        assertEquals(2.0, pieceProblems[1].position.value, "두 번째 문제의 위치는 2.0이어야 합니다")
        assertEquals(3.0, pieceProblems[2].position.value, "세 번째 문제의 위치는 3.0이어야 합니다")
        
        assertEquals(1L, pieceProblems[0].problemId.value, "첫 번째 위치에는 문제 ID 1이 있어야 합니다")
        assertEquals(2L, pieceProblems[1].problemId.value, "두 번째 위치에는 문제 ID 2가 있어야 합니다")
        assertEquals(3L, pieceProblems[2].problemId.value, "세 번째 위치에는 문제 ID 3이 있어야 합니다")
    }
    
    @Test
    fun `createPiece - 존재하지 않는 문제 ID 포함시 NotFoundException 발생`() {
        // Given
        val request = PieceCreateRequest(
            teacherId = 1L,
            title = "수학 학습지",
            problemIds = listOf(1L, 2L, 999L) // 999L은 존재하지 않음
        )
        
        val mockProblems = listOf(
            Problem(1L, "답1", "A", 1, ProblemType.SELECTION),
            Problem(2L, "답2", "B", 2, ProblemType.SUBJECTIVE)
            // 999L은 없음
        )
        
        val savedPiece = Piece(
            id = PieceId(100L),
            teacherId = TeacherId(1L),
            name = PieceName("수학 학습지")
        )
        
        every { pieceRepository.save(any()) } returns savedPiece
        every { problemRepository.findByIds(request.problemIds) } returns mockProblems
        every { pieceProblemRepository.saveAll(any()) } returns emptyList()
        
        // When & Then
        val exception = assertThrows<NotFoundException> {
            pieceCreateService.createPiece(request)
        }
        
        assertEquals(ErrorCode.PROBLEM_NOT_FOUND, exception.errorCode)
        assertEquals("존재하지 않는 문제가 포함되어 있습니다. 누락된 문제 ID: [999]", exception.message)
    }
    
    @Test
    fun `createPiece - 빈 문제 리스트로 생성 시도시 검증 실패`() {
        // Given & When & Then
        assertThrows<IllegalArgumentException> {
            PieceCreateRequest(
                teacherId = 1L,
                title = "빈 학습지",
                problemIds = emptyList()
            )
        }
    }
    
    @Test
    fun `createPiece - 50개 초과 문제로 생성 시도시 검증 실패`() {
        // Given
        val tooManyProblemIds = (1L..51L).toList()
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            PieceCreateRequest(
                teacherId = 1L,
                title = "문제 많은 학습지",
                problemIds = tooManyProblemIds
            )
        }
    }
} 