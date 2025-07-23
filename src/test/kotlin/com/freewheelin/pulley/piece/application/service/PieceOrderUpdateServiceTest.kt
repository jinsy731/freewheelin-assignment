package com.freewheelin.pulley.piece.application.service

import com.freewheelin.pulley.common.domain.PieceId
import com.freewheelin.pulley.common.domain.PieceName
import com.freewheelin.pulley.common.domain.Position
import com.freewheelin.pulley.common.domain.ProblemId
import com.freewheelin.pulley.common.domain.TeacherId
import com.freewheelin.pulley.common.exception.AuthorizationException
import com.freewheelin.pulley.common.exception.ErrorCode
import com.freewheelin.pulley.common.exception.NotFoundException
import com.freewheelin.pulley.piece.application.port.ProblemOrderUpdateCommand
import com.freewheelin.pulley.piece.domain.port.PieceProblemRepository
import com.freewheelin.pulley.piece.domain.port.PieceRepository
import com.freewheelin.pulley.piece.domain.model.Piece
import com.freewheelin.pulley.piece.domain.model.PieceProblem
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * PieceOrderUpdateService 단위 테스트
 */
class PieceOrderUpdateServiceTest {
    
    private val pieceRepository = mockk<PieceRepository>()
    private val pieceProblemRepository = mockk<PieceProblemRepository>()
    
    private val service = PieceOrderUpdateService(
        pieceRepository,
        pieceProblemRepository
    )
    
    @Test
    fun `정상적인 경우 - 맨 앞으로 이동 (prevProblemId=null)`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val problemId = 1003L  // 이동할 문제
        
        val piece = Piece.create(
            teacherId = TeacherId(teacherId),
            name = PieceName("테스트 학습지")
        )
        
        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            problemId = problemId,
            prevProblemId = null,  // 맨 앞으로 이동
            nextProblemId = 1001L
        )
        
        val problemToMove = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(problemId),
            position = Position(0.900000)  // 기존 위치
        )
        
        val nextProblem = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(1001L),
            position = Position(0.100000)
        )
        
        val foundProblems = listOf(problemToMove, nextProblem)
        
        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findByPieceIdAndProblemIdIn(pieceId, listOf(problemId, 1001L)) } returns foundProblems
        every { pieceProblemRepository.save(any()) } returnsArgument 0
        
        // When
        val result = service.updateProblemOrder(command)
        
        // Then
        assertTrue(result.success)
        assertEquals(pieceId, result.pieceId)
        assertEquals(problemId, result.problemId)
        assertEquals(0.900000, result.previousPosition)
        
        // 저장이 호출되었는지 확인
        verify { pieceProblemRepository.save(any()) }
    }
    
    @Test
    fun `정상적인 경우 - 맨 뒤로 이동 (nextProblemId=null)`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val problemId = 1001L  // 이동할 문제
        
        val piece = Piece.create(
            teacherId = TeacherId(teacherId),
            name = PieceName("테스트 학습지")
        )
        
        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            problemId = problemId,
            prevProblemId = 1003L,
            nextProblemId = null  // 맨 뒤로 이동
        )
        
        val problemToMove = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(problemId),
            position = Position(0.100000)  // 기존 위치
        )
        
        val prevProblem = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(1003L),
            position = Position(0.900000)
        )
        
        val foundProblems = listOf(problemToMove, prevProblem)
        
        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findByPieceIdAndProblemIdIn(pieceId, listOf(problemId, 1003L)) } returns foundProblems
        every { pieceProblemRepository.save(any()) } returnsArgument 0
        
        // When
        val result = service.updateProblemOrder(command)
        
        // Then
        assertTrue(result.success)
        assertEquals(pieceId, result.pieceId)
        assertEquals(problemId, result.problemId)
        assertEquals(0.100000, result.previousPosition)
        
        // 저장이 호출되었는지 확인
        verify { pieceProblemRepository.save(any()) }
    }
    
    @Test
    fun `정상적인 경우 - 중간으로 이동 (prevProblemId와 nextProblemId 모두 지정)`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val problemId = 1001L  // 이동할 문제
        
        val piece = Piece.create(
            teacherId = TeacherId(teacherId),
            name = PieceName("테스트 학습지")
        )
        
        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            problemId = problemId,
            prevProblemId = 1002L,
            nextProblemId = 1003L
        )
        
        val problemToMove = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(problemId),
            position = Position(0.100000)  // 기존 위치
        )
        
        val prevProblem = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(1002L),
            position = Position(0.500000)
        )
        
        val nextProblem = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(1003L),
            position = Position(0.900000)
        )
        
        val foundProblems = listOf(problemToMove, prevProblem, nextProblem)
        
        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findByPieceIdAndProblemIdIn(pieceId, listOf(problemId, 1002L, 1003L)) } returns foundProblems
        every { pieceProblemRepository.save(any()) } returnsArgument 0
        
        // When
        val result = service.updateProblemOrder(command)
        
        // Then
        assertTrue(result.success)
        assertEquals(pieceId, result.pieceId)
        assertEquals(problemId, result.problemId)
        assertEquals(0.100000, result.previousPosition)
        
        // 저장이 호출되었는지 확인
        verify { pieceProblemRepository.save(any()) }
    }
    
    @Test
    fun `권한 없음 - 다른 선생님의 학습지 수정 시도`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val requestingTeacherId = 999L  // 다른 선생님
        
        val piece = Piece.create(
            teacherId = TeacherId(teacherId),  // 실제 소유자
            name = PieceName("테스트 학습지")
        )
        
        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = requestingTeacherId,  // 다른 선생님이 요청
            problemId = 1001L,
            prevProblemId = null,
            nextProblemId = null
        )
        
        // Mocking
        every { pieceRepository.getById(pieceId) } returns piece
        
        // When & Then
        assertThrows<AuthorizationException> {
            service.updateProblemOrder(command)
        }
        
        // 저장이 호출되지 않았는지 확인
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }
    
    @Test
    fun `문제 없음 - 이동할 문제가 학습지에 없는 경우`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val problemId = 9999L  // 존재하지 않는 문제
        
        val piece = Piece.create(
            teacherId = TeacherId(teacherId),
            name = PieceName("테스트 학습지")
        )
        
        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            problemId = problemId,
            prevProblemId = null,
            nextProblemId = null
        )
        
        // Mocking - 빈 리스트 반환 (문제가 없음)
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findByPieceIdAndProblemIdIn(pieceId, listOf(problemId)) } returns emptyList()
        
        // When & Then
        assertThrows<NotFoundException> {
            service.updateProblemOrder(command)
        }
        
        // 저장이 호출되지 않았는지 확인
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }
    
    @Test
    fun `이전 문제 없음 - 지정된 prevProblemId가 학습지에 없는 경우`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val problemId = 1001L
        val prevProblemId = 9999L  // 존재하지 않는 이전 문제
        
        val piece = Piece.create(
            teacherId = TeacherId(teacherId),
            name = PieceName("테스트 학습지")
        )
        
        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            problemId = problemId,
            prevProblemId = prevProblemId,
            nextProblemId = null
        )
        
        val problemToMove = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(problemId),
            position = Position(0.500000)
        )
        
        // Mocking - 이동할 문제만 있고 이전 문제는 없음
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findByPieceIdAndProblemIdIn(pieceId, listOf(problemId, prevProblemId)) } returns listOf(problemToMove)
        
        // When & Then
        assertThrows<NotFoundException> {
            service.updateProblemOrder(command)
        }
        
        // 저장이 호출되지 않았는지 확인
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }
    
    @Test
    fun `다음 문제 없음 - 지정된 nextProblemId가 학습지에 없는 경우`() {
        // Given
        val pieceId = 100L
        val teacherId = 1L
        val problemId = 1001L
        val nextProblemId = 9999L  // 존재하지 않는 다음 문제
        
        val piece = Piece.create(
            teacherId = TeacherId(teacherId),
            name = PieceName("테스트 학습지")
        )
        
        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            problemId = problemId,
            prevProblemId = null,
            nextProblemId = nextProblemId
        )
        
        val problemToMove = PieceProblem.create(
            pieceId = PieceId(pieceId),
            problemId = ProblemId(problemId),
            position = Position(0.500000)
        )
        
        // Mocking - 이동할 문제만 있고 다음 문제는 없음
        every { pieceRepository.getById(pieceId) } returns piece
        every { pieceProblemRepository.findByPieceIdAndProblemIdIn(pieceId, listOf(problemId, nextProblemId)) } returns listOf(problemToMove)
        
        // When & Then
        assertThrows<NotFoundException> {
            service.updateProblemOrder(command)
        }
        
        // 저장이 호출되지 않았는지 확인
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }
    
    @Test
    fun `학습지 없음 - 존재하지 않는 학습지 ID`() {
        // Given
        val pieceId = 9999L  // 존재하지 않는 학습지
        val teacherId = 1L
        
        val command = ProblemOrderUpdateCommand(
            pieceId = pieceId,
            teacherId = teacherId,
            problemId = 1001L,
            prevProblemId = null,
            nextProblemId = null
        )
        
        // Mocking - 학습지가 없음
        every { pieceRepository.getById(pieceId) } throws NotFoundException(
            ErrorCode.PIECE_NOT_FOUND,
            "학습지를 찾을 수 없습니다: $pieceId"
        )
        
        // When & Then
        assertThrows<NotFoundException> {
            service.updateProblemOrder(command)
        }
        
        // 저장이 호출되지 않았는지 확인
        verify(exactly = 0) { pieceProblemRepository.save(any()) }
    }
} 